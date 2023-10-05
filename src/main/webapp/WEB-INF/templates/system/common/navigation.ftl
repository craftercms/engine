<#macro navigation
<#---->url="/site/website"
<#---->navElementClass=""
<#---->showNavElement=true
<#---->containerElement="ul"
<#---->containerElementClass=""
<#---->itemWrapperElement="li"
<#---->itemWrapperClass=""
<#---->itemWrapperActiveClass="active"
<#---->itemWrapperAttributes={}
<#---->itemClass=""
<#---->itemActiveClass="active"
<#---->itemAttributes={}
<#---->hasSubItemItemClass=""
<#---->hasSubItemWrapperClass=""
<#---->hasSubItemItemAttributes={}
<#---->subItemClass=""
<#---->subItemClassPrefix="nav-level"
<#---->subItemAttributes={}
<#---->subItemWrapperClass=""
<#---->subItemWrapperClassPrefix=""
<#---->subItemContainerClass=""
<#---->depth=1
<#---->includeRoot=true
<#---->inlineRootWithImmediateChildren=true
>
  <#assign navTree = navTreeBuilder.getNavTree(url, depth, Request.pageUrl)/>
  <#-- navElement will be rendered if: showNavElement = true -->
  <#if showNavElement != false><nav <#if navElementClass != ''>class="${navElementClass}"</#if>></#if>

  <#-- containerElement will be rendered if containerElement has no empty value, and includeRoot is set to true -->
  <#-- if includeRoot is false, then the first container will be the one containing the root subitems -->
  <#if (containerElement != "") && (includeRoot)><${containerElement} <#if containerElementClass != ''>class="${containerElementClass}"</#if>></#if>
    <@navigationItem
      containerElement=containerElement
      containerElementClass=containerElementClass
      itemWrapperElement=itemWrapperElement
      itemWrapperClass=itemWrapperClass
      itemWrapperActiveClass=itemWrapperActiveClass
      itemWrapperAttributes=itemWrapperAttributes
      itemClass=itemClass
      itemActiveClass=itemActiveClass
      itemAttributes=itemAttributes
      hasSubItemItemClass=hasSubItemItemClass
      hasSubItemWrapperClass=hasSubItemWrapperClass
      hasSubItemItemAttributes=hasSubItemItemAttributes
      subItemClass=subItemClass
      subItemClassPrefix=subItemClassPrefix
      subItemAttributes=subItemAttributes
      subItemWrapperClass=subItemWrapperClass
      subItemWrapperClassPrefix=subItemWrapperClassPrefix
      subItemContainerClass=subItemContainerClass
      depth=depth
      currentDepth=0
      navItem=navTree
      includeRoot=includeRoot
      inlineRootWithImmediateChildren=inlineRootWithImmediateChildren
    />
  <#if containerElement != ""></${containerElement}></#if>
  <#if showNavElement != false></nav></#if>
</#macro>

<#macro navigationItem
<#---->containerElement="ul"
<#---->containerElementClass=""
<#---->itemWrapperElement="li"
<#---->itemWrapperClass=""
<#---->itemWrapperActiveClass="active"
<#---->itemWrapperAttributes={}
<#---->itemClass=""
<#---->itemActiveClass="active"
<#---->itemAttributes={}
<#---->hasSubItemItemClass=""
<#---->hasSubItemWrapperClass=""
<#---->hasSubItemItemAttributes={}
<#---->subItemClass=""
<#---->subItemClassPrefix="nav-level"
<#---->subItemAttributes={}
<#---->subItemWrapperClass=""
<#---->subItemWrapperClassPrefix=""
<#---->subItemContainerClass=""
<#---->depth=1
<#---->currentDepth=0
<#---->navItem={}
<#---->includeRoot=true
<#---->inlineRootWithImmediateChildren=true
>
  <#-- itemWrapperElement will be rendered if itemWrapperElement has no empty value and rootItem is set to true -->
  <#-- if no rootItem is rendered, then the itemWrapper is not rendered (it would be empty) -->
  <#if itemWrapperElement != "" && includeRoot>
    <#assign hasSubItems = ((navItem.subItems)?size > 0) />
    <#assign addSubItemData = hasSubItems && currentDepth < depth && !inlineRootWithImmediateChildren/>
    <#assign itemWrapperDepthClass = (currentDepth == 0)?then(
      itemWrapperClass,
      '${subItemWrapperClass} ${(subItemWrapperClassPrefix != "")?then("${subItemClassPrefix}-${currentDepth}", "")}'
    )/>

    <${itemWrapperElement}
      class="${navItem.active?then(itemWrapperActiveClass, '')} ${itemWrapperDepthClass} ${(addSubItemData && hasSubItemWrapperClass != '')?then(hasSubItemWrapperClass, '')}"
      <#list itemWrapperAttributes as attr, value>
        ${attr}="${value}"
      </#list>
    >
  </#if>
  <#if ((currentDepth == 0) && includeRoot) || (currentDepth > 0)>
    <#if modePreview>
      <#assign storeUrl = urlTransformationService.transform('renderUrlToStoreUrl', navItem.url)>
      <#assign item = siteItemService.getSiteItem(storeUrl) />
    </#if>
    <#assign itemDepthClass = (currentDepth == 0)?then(
      itemClass,
      '${subItemClass} ${(subItemClassPrefix != "")?then("${subItemClassPrefix}-${currentDepth}", "")}'
    )/>

    <@a
      class="${navItem.active?then(itemActiveClass, '')} ${(addSubItemData)?then(hasSubItemItemClass, itemDepthClass)}"
      $model=item
      href="${navItem.url}"
      $attributes=(addSubItemData)?then(hasSubItemItemAttributes, itemAttributes)
    >
    <#outputFormat "HTML">
      ${navItem.label}
    </#outputFormat>
    </@a>
  </#if>

  <#-- itemWrapperElement will not be rendered if itemWrapperElement is not empty and if root and root first level
  children are set to be in same level-->
  <#if (itemWrapperElement != "") && (inlineRootWithImmediateChildren)></${itemWrapperElement}></#if>
  <#assign subItems = navItem.subItems/>
  <#-- if current item has subitems: -->
  <#if (depth > 0) && (currentDepth < depth) && (subItems?size > 0)>
    <#if ((containerElement != "") && (!inlineRootWithImmediateChildren) || !includeRoot)>
      <${containerElement}
        class="${(currentDepth == 0 && !includeRoot)?then(containerElementClass, subItemContainerClass)}"
      >
    </#if>
    <#list subItems as subItem>
      <@navigationItem
        containerElement=containerElement
        itemWrapperElement=itemWrapperElement
        itemWrapperClass=itemWrapperClass
        itemWrapperActiveClass=itemWrapperActiveClass
        itemWrapperAttributes=itemWrapperAttributes
        itemClass=itemClass
        itemActiveClass=itemActiveClass
        itemAttributes=itemAttributes
        hasSubItemItemClass=hasSubItemItemClass
        hasSubItemWrapperClass=hasSubItemWrapperClass
        hasSubItemItemAttributes=hasSubItemItemAttributes
        subItemClass=subItemClass
        subItemClassPrefix=subItemClassPrefix
        subItemWrapperClass=subItemWrapperClass
        subItemWrapperClassPrefix=subItemWrapperClassPrefix
        subItemContainerClass=subItemContainerClass
        depth=(inlineRootWithImmediateChildren && currentDepth == 0)?then(depth - 1, currentDepth +1)
        currentDepth=(inlineRootWithImmediateChildren && currentDepth == 0)?then(currentDepth, currentDepth + 1)
        navItem=subItem
        inlineRootWithImmediateChildren=false
      />
    </#list>
    <#if (containerElement != "") && (!inlineRootWithImmediateChildren)></${containerElement}></#if>
  </#if>
  <#if (itemWrapperElement != "")  && (!inlineRootWithImmediateChildren)></${itemWrapperElement}></#if>
</#macro>
