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
  <#-- containerElement will be rendered if containerElement has no empty value -->
  <#if (containerElement != "")><${containerElement} class="${containerElementClass}"></#if>
    <#list breadcrumb as item>
      <#-- itemWrapperElement will not be rendered if itemWrapperElement has no empty value -->
      <#if (itemWrapperElement != "")>
        <${itemWrapperElement}
          class="${(item.active)?then(itemWrapperActiveClass, '')} ${itemWrapperClass}"
          <#list itemWrapperAttributes as attr, value>
            ${attr}="${value}"
          </#list>
        >
      </#if>
        <#if modePreview>
          <#assign storeUrl = urlTransformationService.transform('renderUrlToStoreUrl', item.url)>
          <#assign siteItem = siteItemService.getSiteItem(storeUrl) />
        </#if>

        <#if !(item.active) || includeLinkInActiveItem>
          <@a
            $model=siteItem
            class="${itemClass}"
            href="${item.url}"
            $attributes=itemAttributes
          >

            <#outputFormat "HTML">
              ${item.label}
            </#outputFormat>
          </@a>
        <#else>
          <@span $model=siteItem $attributes=itemAttributes>
            <#outputFormat "HTML">
              ${item.label}
            </#outputFormat>
          </@span>
        </#if>
      <#if (itemWrapperElement != "")>
        </${itemWrapperElement}>
      </#if>
    </#list>
  <#if (containerElement != "")></${containerElement}></#if>
  <#if showNavElement != false></nav></#if>
</#macro>
