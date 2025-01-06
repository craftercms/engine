<#include "./layout.ftl">

<#if (errorMessage! "") == "PreviewAccessException">
  <#assign previewException=true>
  <#assign resolvedErrorMessage>
      A Preview Token is required to access project content in the preview environment. You can read more about Preview Tokens
      <a href="https://craftercms.com/docs/current/reference/modules/studio/administration.html#preview-token" target="_blank">here</a>.
      <br /><br />
      Note: Consuming content via the preview server is intended to support authoring use cases. Production applications (and applications running in other environments) should consume content published to those environments and delivered by Crafter Engine delivery servers for that environment.
  </#assign>
<#else>
  <#assign previewException=false>
  <#assign resolvedErrorMessage>The user couldn't be authenticated correctly.</#assign>
</#if>

<@layout
  pageTitle="Authentication required"
  errorCode="401"
  errorMessage="${resolvedErrorMessage}"
  paddedErrorMessage=previewException
/>