<#include "./layout.ftl">

<@layout
  pageTitle="Internal Server Error"
  errorCode="500"
  errorMessage="Oops! A server error has occurred and we were unable to fulfill the request.<br/> Please try again. If the error persists, contact the administrator of the site."
  stackTrace="${stackTrace}"
/>
