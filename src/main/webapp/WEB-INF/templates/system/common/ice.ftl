<#macro initPageBuilder isAuthoring=(modePreview) addReact=false props="{}" other...>
<#if isAuthoring>
<!--
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
Crafter CMS Authoring Scripts
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
-->
<script src="/studio/static-assets/modules/editors/tinymce/v5/tinymce/tinymce.min.js"></script>
<#if addReact>
<#-- TODO: Import minified script -->
<#else>
<#-- TODO: Create Reactless build -->
<script src="/studio/static-assets/scripts/craftercms-guest.umd.js"></script>
</#if>
<script>
  window.craftercms.guest.initPageBuilder({
    path: '${model.getItem().descriptorUrl!''}',
    modelId: '${model.objectId!''}',
    ...${props}
  });
</script>
<!--
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
-->
</#if>
</#macro>

<#function mergeAttributes attrs $attrs>
  <#assign attributes = attrs?has_content?then(attrs, {})>
  <#assign $attributes = attributes + $attrs>
  <#return attributes + $attrs>
</#function>

<#macro tag $tag="div" $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign nested><#nested/></#assign>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <#if nested?has_content>
    <#assign nested = "\n${nested}  ">
  </#if>
  <${$tag}
  <#list $attributes as attr, value>
    ${attr}="${value}"
  </#list>
  <#if modePreview && $model?has_content>
    data-craftercms-model-path="${$model.storeUrl!"__PATH_NOT_FOUND__"}"
    data-craftercms-model-id="${$model.objectId!"__ID_NOT_FOUND__"}"
    <#if $field?has_content>
    <#---->data-craftercms-field-id="${$field}"
    </#if>
    <#if $index?has_content>
    <#---->data-craftercms-index="${$index}"
    </#if>
    <#if $label?has_content>
    <#---->data-craftercms-label="${$label}"
    </#if>
  </#if>
  >${nested}</${$tag}>
</#macro>

<#macro article $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="article" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro a $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="a" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro img $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="img" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro header $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="header" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro footer $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="footer" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro div $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="div" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro section $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
    <@tag $tag="section" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro span $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="span" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro h1 $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="h1" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro h2 $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="h2" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro h3 $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="h3" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro h4 $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="h4" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro h5 $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="h5" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro h6 $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="h6" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro ul $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="ul" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro html $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="html" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro body $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="body" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro head $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="head" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro p $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="p" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro ul $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="ul" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro ol $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="ol" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro li $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="li" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro iframe $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="iframe" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro em $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="em" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro strong $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="strong" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro b $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="em" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro i $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="i" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro small $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="small" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro em $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="th" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro caption $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="caption" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro tr $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="tr" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro td $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="td" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro abbr $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="abbr" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro address $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="address" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro aside $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="aside" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro audio $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="audio" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro video $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="video" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro em $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="em" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro blockquote $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="blockquote" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro cite $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="cite" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro em $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="em" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro code $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="code" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro nav $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="nav" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro em $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="em" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro figure $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="figure" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro figcaption $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="figcaption" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro pre $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="pre" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro time $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="time" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro map $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="map" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro picture $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="picture" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro source $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="source" $model=$model $field=$field $index=$index $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro componentRootTag $tag="div" $model=($model!contentModel) $field=($field!"") $label=($label!"") $attrs={} attrs...>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag=$tag $model=$model $field=$field $label=$label $attrs=$attributes><#nested></@tag>
</#macro>

<#macro renderComponentCollection
<#---->$field
<#---->$tag="div"
<#---->$itemTag="div"
<#---->$model=contentModel
<#---->$attrs={}
<#---->$itemAttrs={}
<#---->arguments={}
<#---->attrs...
>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <#-- Field container element -->
  <@tag $tag=$tag $field=$field $model=$model $attrs=attributes>
    <#if $model[$field]?? && $model[$field].item??>
      <#list $model[$field].item as item>
        <#-- Item container element -->
        <@tag $tag=$itemTag $model=$model $field=$field $index=item?index $attrs=$itemAttrs>
          <#-- Component element -->
          <@renderComponent component=item additionalModel=arguments />
        </@tag>
      </#list>
    </#if>
  </@tag>
</#macro>

<#macro renderRepeatCollection
<#---->$field
<#---->$model=contentModel
<#---->$containerTag="ul"
<#---->$containerAttributes={}
<#---->$itemTag="li"
<#---->$itemAttributes={}
>
  <#-- Field container element -->
  <@tag $model=$model $field=$field $index="" $tag=$containerTag $attrs=$containerAttributes>
    <#if $model[$field]?? && $model[$field].item??>
      <#list $model[$field].item as item>
        <#assign index = item?index>
        <#-- Item container element -->
        <@tag
        <#---->$model=$model
        <#---->$field=$field
        <#---->$index=index
        <#---->$tag=$itemTag
        <#---->$attrs=$itemAttributes
        >
          <#nested item, index>
        </@tag>
      </#list>
    </#if>
  </@tag>
</#macro>

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
    <#assign storeUrl = urlTransformationService.transform('renderUrlToStoreUrl', navItem.url)>
    <#assign item = siteItemService.getSiteItem(storeUrl) />
    <#assign itemDepthClass = (currentDepth == 0)?then(
      itemClass,
      '${subItemClass} ${(subItemClassPrefix != "")?then("${subItemClassPrefix}-${currentDepth}", "")}'
    )/>

    <@a
      class="${navItem.active?then(itemActiveClass, '')} ${(addSubItemData)?then(hasSubItemItemClass, itemDepthClass)}"
      $model=item
      href="${navItem.url}"
      $attrs=(addSubItemData)?then(hasSubItemItemAttributes, itemAttributes)
    >
      ${navItem.label}
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