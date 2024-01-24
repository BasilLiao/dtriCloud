package dtri.com.tw.login;

//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//	@Autowired
//	private JwtUtilities jwtUtilities;
//	@Autowired
//	private CutomerUserDetailsService customerUserDetailsService;
//	private Logger log = LoggerFactory.getLogger(this.getClass());
//
//	/**
//	 * 請求訪問-攔截-查看token->
//	 * 
//	 */
//	@Override
//	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
//			throws ServletException, IOException {
//		// 攔截請求-> 如果有取得 令牌(Token)->跑JWT程序->驗證令牌->有此使用者->則不需要驗證
//		// 攔截請求-> 如果沒有 令牌(Token)->繼續 跑SpringSecurity程序->身分驗證
//		String token = jwtUtilities.getToken(request);
//		if (token != null && jwtUtilities.validateToken(token)) {
//			String userAccount = jwtUtilities.extractUsername(token);
//			// 取得該用戶
//			UserDetails userDetails = customerUserDetailsService.loadUserByUsername(userAccount);
//			if (userDetails != null) {
//				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null,
//						userDetails.getAuthorities());
//				log.info("authenticated user with userAccount :{}", userAccount);
//				SecurityContextHolder.getContext().setAuthentication(authentication);
//
//			}
//		}
//		filterChain.doFilter(request, response);
//	}
//}
