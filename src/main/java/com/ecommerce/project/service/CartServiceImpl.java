    package com.ecommerce.project.service;

    import com.ecommerce.project.exceptions.APIException;
    import com.ecommerce.project.exceptions.ResourceNotFoundException;
    import com.ecommerce.project.model.Cart;
    import com.ecommerce.project.model.CartItem;
    import com.ecommerce.project.model.Product;
    import com.ecommerce.project.payload.CartDTO;
    import com.ecommerce.project.payload.CartItemDTO;
    import com.ecommerce.project.payload.ProductDTO;
    import com.ecommerce.project.repositories.CartItemRepository;
    import com.ecommerce.project.repositories.CartRepository;
    import com.ecommerce.project.repositories.ProductRepository;
    import com.ecommerce.project.util.AuthUtil;
    import jakarta.transaction.Transactional;
    import org.modelmapper.ModelMapper;
    import org.springframework.stereotype.Service;

    import java.util.List;
    import java.util.stream.Stream;

    @Service
    @Transactional
    public class CartServiceImpl implements CartService{

        private static final String PRODUCT_ENTITY = "Product";
        private static final String PRODUCT_ID_FIELD = "productId";
        private static final String CART_ENTITY = "Cart";
        private static final String CART_ID_FIELD = "cartId";

        private final CartRepository cartRepository;
        private final AuthUtil authUtil;
        private final ProductRepository productRepository;
        private final CartItemRepository cartItemRepository;
        private final ModelMapper modelMapper;

        public CartServiceImpl(CartRepository cartRepository, AuthUtil authUtil, ProductRepository productRepository, CartItemRepository cartItemRepository, ModelMapper modelMapper) {
            this.cartRepository = cartRepository;
            this.authUtil = authUtil;
            this.productRepository = productRepository;
            this.cartItemRepository = cartItemRepository;
            this.modelMapper = modelMapper;
        }

        @Override
        public CartDTO addProductToCart(Long productId, Integer quantity) {
            Cart cart  = createCart();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_ENTITY, PRODUCT_ID_FIELD, productId));

            CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

            if (cartItem != null) {
                throw new APIException(PRODUCT_ENTITY +" "+ product.getProductName() + " already exists in the cart");
            }

            if (product.getQuantity() == 0) {
                throw new APIException(product.getProductName() + " is not available");
            }

            if (product.getQuantity() < quantity) {
                throw new APIException("Please, make an order of the " +" "+ product.getProductName()
                        + " less than or equal to the quantity " + product.getQuantity() + ".");
            }

            CartItem newCartItem = new CartItem();

            newCartItem.setProduct(product);
            newCartItem.setCart(cart);
            newCartItem.setQuantity(quantity);
            newCartItem.setDiscount(product.getDiscount());
            newCartItem.setProductPrice(product.getSpecialPrice());

            cartItemRepository.save(newCartItem);

            product.setQuantity(product.getQuantity());

            cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

            cartRepository.save(cart);

            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<CartItem> cartItems = cart.getCartItems();

            Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
                ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                map.setQuantity(item.getQuantity());
                return map;
            });

            cartDTO.setProducts(productStream.toList());

            return cartDTO;
        }

        @Override
        public List<CartDTO> getAllCarts() {
            List<Cart> carts = cartRepository.findAll();

            // 🔧 MODIFIED: Sonar recommends using isEmpty() instead of size() == 0
            if (carts.isEmpty()) {
                throw new APIException("No cart exists");
            }

            // 🔧 MODIFIED: directly return the stream result instead of using a temporary variable
            return carts.stream().map(cart -> {
                CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

                // 🔧 MODIFIED: collect(Collectors.toList()) ➜ toList()
                List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                    ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(cartItem.getQuantity()); // Set the quantity from CartItem
                    return productDTO;
                }).toList(); // Sonar-compliant

                cartDTO.setProducts(products);
                return cartDTO;
            }).toList(); // Sonar-compliant// was assigned to cartDTOs
        }

        @Override
        @Transactional
        public CartDTO getCart(String emailId, Long cartId) {
            Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
            if (cart == null){
                throw new ResourceNotFoundException(CART_ENTITY, CART_ID_FIELD, cartId);
            }
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            cart.getCartItems().forEach(c ->
                    c.getProduct().setQuantity(c.getQuantity()));
            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                    .toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }

        @Transactional
        @Override
        public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

            String emailId = authUtil.loggedInEmail();
            Cart userCart = cartRepository.findCartByEmail(emailId);
            Long cartId  = userCart.getCartId();

            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException(CART_ENTITY, CART_ID_FIELD, cartId));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_ENTITY, PRODUCT_ID_FIELD, productId));

            if (product.getQuantity() == 0) {
                throw new APIException(product.getProductName() + " is not available");
            }

            if (product.getQuantity() < quantity) {
                throw new APIException("Please, make an order of the " + product.getProductName()
                        + " less than or equal to the quantity " + product.getQuantity() + ".");
            }

            CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

            if (cartItem == null) {
                throw new APIException(PRODUCT_ENTITY +" "+ product.getProductName() + " not available in the cart!!!");
            }

            // Calculate new quantity
            int newQuantity = cartItem.getQuantity() + quantity;

            // Validation to prevent negative quantities
            if (newQuantity < 0) {
                throw new APIException("The resulting quantity cannot be negative.");
            }

            if (newQuantity == 0){
                deleteProductFromCart(cartId, productId);
            } else {
                cartItem.setProductPrice(product.getSpecialPrice());
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItem.setDiscount(product.getDiscount());
                cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
                cartRepository.save(cart);
            }

            CartItem updatedItem = cartItemRepository.save(cartItem);
            if(updatedItem.getQuantity() == 0){
                cartItemRepository.deleteById(updatedItem.getCartItemId());
            }


            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<CartItem> cartItems = cart.getCartItems();

            Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
                ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
                prd.setQuantity(item.getQuantity());
                return prd;
            });


            cartDTO.setProducts(productStream.toList());

            return cartDTO;
        }


        private Cart createCart() {
            Cart userCart  = cartRepository.findCartByEmail(authUtil.loggedInEmail());
            if(userCart != null){
                return userCart;
            }

            Cart cart = new Cart();
            cart.setTotalPrice(0.00);
            cart.setUser(authUtil.loggedInUser());
            // 🔧 MODIFIED: directly return the repository call
            return cartRepository.save(cart);
        }

        @Transactional
        @Override
        public String deleteProductFromCart(Long cartId, Long productId) {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException(CART_ENTITY, CART_ID_FIELD, cartId));

            CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

            if (cartItem == null) {
                throw new ResourceNotFoundException(PRODUCT_ENTITY, PRODUCT_ID_FIELD, productId);
            }

            cart.setTotalPrice(cart.getTotalPrice() -
                    (cartItem.getProductPrice() * cartItem.getQuantity()));

            cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

            return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
        }


        @Override
        public void updateProductInCarts(Long cartId, Long productId) {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException(CART_ENTITY, CART_ID_FIELD, cartId));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_ENTITY, PRODUCT_ID_FIELD, productId));

            CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

            if (cartItem == null) {
                throw new APIException(PRODUCT_ENTITY +" "+ product.getProductName() + " not available in the cart!!!");
            }

            double cartPrice = cart.getTotalPrice()
                    - (cartItem.getProductPrice() * cartItem.getQuantity());

            cartItem.setProductPrice(product.getSpecialPrice());

            cart.setTotalPrice(cartPrice
                    + (cartItem.getProductPrice() * cartItem.getQuantity()));

            // 🔧 FIX: no reassignment
            cartItemRepository.save(cartItem);
        }

        @Transactional
        @Override
        public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {
            // Get user's email
            String emailId = authUtil.loggedInEmail();

            // Check if an existing cart is available or create a new one
            Cart existingCart = cartRepository.findCartByEmail(emailId);
            if (existingCart == null) {
                existingCart = new Cart();
                existingCart.setTotalPrice(0.00);
                existingCart.setUser(authUtil.loggedInUser());
                existingCart = cartRepository.save(existingCart);
            } else {
                // Clear all current items in the existing cart
                cartItemRepository.deleteAllByCartId(existingCart.getCartId());
            }

            double totalPrice = 0.00;

            // Process each item in the request to add to the cart
            for (CartItemDTO cartItemDTO : cartItems) {
                Long productId = cartItemDTO.getProductId();
                Integer quantity = cartItemDTO.getQuantity();

                // Find the product by ID
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_ENTITY, PRODUCT_ID_FIELD, productId));

                // Directly update product stock and total price

                totalPrice += product.getSpecialPrice() * quantity;

                // Create and save cart item
                CartItem cartItem = new CartItem();
                cartItem.setProduct(product);
                cartItem.setCart(existingCart);
                cartItem.setQuantity(quantity);
                cartItem.setProductPrice(product.getSpecialPrice());
                cartItem.setDiscount(product.getDiscount());
                cartItemRepository.save(cartItem);
            }

            // Update the cart's total price and save
            existingCart.setTotalPrice(totalPrice);
            cartRepository.save(existingCart);
            return "Cart created/updated with the new items successfully";
        }

    }
