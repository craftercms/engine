if (!authentication) {
   response.sendError(400, "You're not a subscriber")
} else {
    filterChain.doFilter(request, response)
}
