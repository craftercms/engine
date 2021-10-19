<#macro initInContextEditing isAuthoring=(modePreview) addReact=false props="{}" other...>
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
  window.craftercms.guest.initInContextEditing({
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
  <#if ["img","link","br","hr","area","col","hr","meta","source","track","input"]?seq_contains($tag)>
  >
  <#else>
  >${nested}</${$tag}>
  </#if>
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
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$fieldCarryover=""
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$indexCarryover=""
<#---->$tag="div"
<#---->$itemTag="div"
<#---->$model=contentModel
<#---->$collection=$model[$field]
<#---->$attrs={}
<#---->$itemAttrs={}
<#---->arguments={}
<#---->attrs...
>
  <#assign attributes = mergeAttributes(attrs, $attrs)>
  <#-- Field container element -->
  <@tag
    $tag=$tag
    $field=cleanDotNotationString("${$fieldCarryover}.${$field}")
    $index=$indexCarryover
    $model=$model $attrs=attributes
  >
    <#if $collection?? && $collection.item??>
      <#list $collection.item as item>
        <#-- Item container element -->
        <@tag
          $tag=$itemTag
          $model=$model
          $field=cleanDotNotationString("${$fieldCarryover}.${$field}")
          $index=cleanDotNotationString("${$indexCarryover}.${item?index}")
          $attrs=$itemAttrs
        >
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
        <#---->$index="${index}"
        <#---->$tag=$itemTag
        <#---->$attrs=$itemAttributes
        >
          <#nested item, index>
        </@tag>
      </#list>
    </#if>
  </@tag>
</#macro>

<#function cleanDotNotationString str>
  <#return str?replace('^[.]+|[.]+$', '', 'r')?replace('[.]{2,}', '.', 'r')>
</#function>

<#function isEmptyCollection collection>
    <#return collection?has_content && collection.item?has_content>
</#function>

<#function shouldAddEmptyStyles collection>
    <#return modePreview && !isEmptyCollection(collection)>
</#function>

<#function printIfIsEmptyCollection collection output="craftercms-is-empty">
    <#return shouldAddEmptyStyles(collection)?then(output, '')>
</#function>

<#function printIfPreview output>
    <#return modePreview?then(output, '')>
</#function>
