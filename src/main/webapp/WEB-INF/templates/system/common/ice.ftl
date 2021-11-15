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

<#macro tag $tag="div" $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign nested><#nested/></#assign>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <#if nested?has_content>
    <#assign nested = "\n${nested}  ">
  </#if>
  <${$tag}
  <#list mergedAttributes as attr, value>
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

<#macro article $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="article" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro a $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="a" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro img $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="img" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro header $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="header" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro footer $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="footer" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro div $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="div" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro section $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
    <@tag $tag="section" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro span $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="span" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro h1 $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h1" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro h2 $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h2" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro h3 $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h3" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro h4 $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h4" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro h5 $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h5" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro h6 $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h6" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro ul $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="ul" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro html $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="html" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro body $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="body" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro head $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="head" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro meta $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
    <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
    <@tag $tag="meta" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro title $model=contentModel $field="" $index="" $label="" $attrs={} attrs...>
    <#assign mergedAttributes = mergeAttributes(attrs, $attrs)>
    <@tag $tag="title" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro p $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="p" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro ul $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="ul" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro ol $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="ol" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro li $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="li" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro iframe $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="iframe" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro em $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="em" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro strong $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="strong" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro b $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="b" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro i $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="i" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro small $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="small" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro th $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="th" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro caption $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="caption" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro tr $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="tr" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro td $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="td" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro table $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
    <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
    <@tag $tag="table" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro abbr $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="abbr" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro address $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="address" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro aside $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="aside" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro audio $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="audio" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro video $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="video" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro blockquote $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="blockquote" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro cite $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="cite" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro code $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="code" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro nav $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="nav" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro figure $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="figure" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro figcaption $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="figcaption" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro pre $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="pre" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro time $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="time" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro map $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="map" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro picture $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="picture" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro source $model=contentModel $field="" $index="" $label="" $attributes={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="source" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes><#nested></@tag>
</#macro>

<#macro renderCollection
<#---->$field
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$fieldCarryover=""
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$indexCarryover=""
<#---->$containerTag="div"
<#---->$itemTag="div"
<#---->$model=contentModel
<#---->$collection=$model[$field]
<#---->$containerAttributes={}
<#---->$itemAttributes={}
<#---->$nthItemAttributes={}
>
  <#-- Field container element -->
  <@tag
    $tag=$containerTag
    $field=cleanDotNotationString("${$fieldCarryover}.${$field}")
    $index=$indexCarryover
    $model=$model
    $attributes=$containerAttributes
  >
    <@forEach $collection; item, index>
      <#local additionalAttributes = $nthItemAttributes["${index}"]!{} />
      <#-- Item container element -->
      <@tag
        $tag=$itemTag
        $model=$model
        $field=cleanDotNotationString("${$fieldCarryover}.${$field}")
        $index=cleanDotNotationString("${$indexCarryover}.${index}")
        $attributes=($itemAttributes + additionalAttributes)
      >
        <#nested item, index>
      </@tag>
    </@forEach>
  </@tag>
</#macro>

<#macro renderComponentCollection
<#---->$field
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$fieldCarryover=""
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$indexCarryover=""
<#---->$containerTag="div"
<#---->$itemTag="div"
<#---->$model=contentModel
<#---->$collection=$model[$field]
<#---->$containerAttributes={}
<#---->$itemAttributes={}
<#---->$nthItemAttributes={}
<#---->renderComponentArguments={}
>
  <@renderCollection
    $field=$field
    $fieldCarryover=$fieldCarryover
    $indexCarryover=$indexCarryover
    $containerTag=$containerTag
    $itemTag=$itemTag
    $model=$model
    $collection=$collection
    $containerAttributes=$containerAttributes
    $itemAttributes=$itemAttributes
    $nthItemAttributes=$nthItemAttributes;
    item, index
  >
    <@renderComponent component=item additionalModel=renderComponentArguments />
  </@renderCollection>
</#macro>

<#macro renderRepeatGroup
<#---->$field
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$fieldCarryover=""
<#-- Used when rendering nested node selectors (e.g. a node selector inside of a repeat group)
------>$indexCarryover=""
<#---->$containerTag="div"
<#---->$itemTag="div"
<#---->$model=contentModel
<#---->$collection=$model[$field]
<#---->$containerAttributes={}
<#---->$itemAttributes={}
<#---->$nthItemAttributes={}
>
  <@renderCollection
    $field=$field
    $fieldCarryover=$fieldCarryover
    $indexCarryover=$indexCarryover
    $containerTag=$containerTag
    $itemTag=$itemTag
    $model=$model
    $collection=$collection
    $containerAttributes=$containerAttributes
    $itemAttributes=$itemAttributes
    $nthItemAttributes=$nthItemAttributes;
    item, index
  >
    <#nested item, index>
  </@renderCollection>
</#macro>

<#macro forEach collection>
  <#if collection?? && collection.item??>
    <#list collection.item as item>
      <#local index = item?index>
      <#nested item, index>
    </#list>
  </#if>
</#macro>

<#function cleanDotNotationString str>
  <#return str?replace('^[.]+|[.]+$', '', 'r')?replace('[.]{2,}', '.', 'r')>
</#function>

<#function isEmptyCollection collection>
    <#return !(collection?has_content && collection.item?has_content)>
</#function>

<#function shouldAddEmptyStyles collection>
    <#return modePreview && isEmptyCollection(collection)>
</#function>

<#function printIfIsEmptyCollection collection output="craftercms-is-empty">
    <#return shouldAddEmptyStyles(collection)?then(output, '')>
</#function>

<#function printIfPreview output>
    <#return modePreview?then(output, '')>
</#function>
