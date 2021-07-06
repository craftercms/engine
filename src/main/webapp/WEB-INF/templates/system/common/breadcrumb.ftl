<#macro breadcrumb
<#---->url = "/site/website"
<#---->root = "/site/website"
<#---->navElementClass=""
<#---->navElementAttributes={}
<#---->showNavElement=true
<#---->containerElement="ul"
<#---->containerElementClass=""
<#---->itemWrapperElement="li"
<#---->itemWrapperClass=""
<#---->itemWrapperActiveClass="active"
<#---->itemWrapperAttributes={}
<#---->itemClass=""
<#---->itemAttributes={}
<#---->includeLinkInActiveItem=false
>
  <#assign breadcrumb = navBreadcrumbBuilder.getBreadcrumb(url, root)>

  <#-- navElement will be rendered if: showNavElement = true -->
  <#if showNavElement != false>
    <nav
      <#if navElementClass != ''>class="${navElementClass}"</#if>
      <#list navElementAttributes as attr, value>
        ${attr}="${value}"
      </#list>
    >
  </#if>
  <#if (containerElement != "")><${containerElement} class="${containerElementClass}"></#if>
    <#list breadcrumb as item>
      <#if (itemWrapperElement != "")>
        <${itemWrapperElement}
          class="${(item.active)?then(itemWrapperActiveClass, '')} ${itemWrapperClass}"
          <#list itemWrapperAttributes as attr, value>
            ${attr}="${value}"
          </#list>
        >
      </#if>
        <#if !(item.active) || includeLinkInActiveItem>
          <a
            class="${itemClass}"
            href="${item.url}"
            <#list itemAttributes as attr, value>
              ${attr}="${value}"
            </#list>
          >
        </#if>
          ${item.label}
        <#if !(item.active) || includeLinkInActiveItem></a></#if>
      <#if (itemWrapperElement != "")>
        </${itemWrapperElement}>
      </#if>
    </#list>
  <#if (containerElement != "")></${containerElement}></#if>
  <#if showNavElement != false></nav></#if>
</#macro>