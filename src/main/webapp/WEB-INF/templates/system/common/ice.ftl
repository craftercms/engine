<#macro initExperienceBuilder isAuthoring=(modePreview) props="{}" other...>
<#assign noXb = ((requestParameters["xb"]!'') == 'off')>
<#if isAuthoring && model?? && !noXb>
<!-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>
CrafterCMS Authoring Scripts
>>>>>>>>>>>>>>>>>>>>>>>>>>>> -->
<script defer src="/studio/static-assets/modules/editors/tinymce/v5/tinymce/tinymce.min.js"></script>
<script defer src="/studio/static-assets/scripts/craftercms-xb.umd.js"></script>
<script>
  document.addEventListener('craftercms.xb:loaded', () => {
    window.craftercms.xb.initExperienceBuilder({
      path: '${model.getItem().descriptorUrl!''}',
      ...${props}
    });
  });
</script>
<!-- <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<< -->
</#if>
</#macro>

<#macro initInContextEditing args...>
  <#if modePreview>
    <p style="font-size: 2em; text-align: center; color: red; margin: 1em;">
      Developer note: <em>initInContextEditing</em> was renamed to <em>initExperienceBuilder</em>. Please update your freemarker template.
    </p>
  </#if>
</#macro>

<#function mergeAttributes attrs $attrs>
  <#assign attributes = attrs?has_content?then(attrs, {})>
  <#assign $attributes = attributes + $attrs>
  <#return attributes + $attrs>
</#function>

<#macro tag
  $tag="div"
  $model=contentModel
  $field=""
  $index=""
  $label=""
  renderEmpty=true
  defaultValue=""
  $attributes={}
  attrs...
>
  <#local nested><#nested/></#local>
  <#local mergedAttributes = mergeAttributes(attrs, $attributes)>
  <#local renderTo = "">
  <#if notEmptyString(nested)>
    <#local renderTo = "innerHTML">
  <#elseif $field?has_content && $field?matches('^([a-zA-Z_-]+)(:{1}.*)$')>
    <#local fieldExpressionParts = $field?split(":")>
    <#local $field = fieldExpressionParts[0]>
    <#local renderTo = fieldExpressionParts[1]?has_content?then(fieldExpressionParts[1], "innerHTML")>
    <#local nested = $model[$field]!>
  </#if>
  <#if renderEmpty && isEmptyString(nested) && notEmptyString(defaultValue)>
    <#local nested = defaultValue>
  </#if>
  <#if renderEmpty || notEmptyString(nested)>
    <${$tag}
      <#if renderTo != "" && renderTo != "innerHTML">
        ${renderTo}="${nested}"
      </#if>
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
    >${(renderTo == "innerHTML")?then(nested, "")}</${$tag}>
    </#if>
  </#if>
</#macro>

<#macro article $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="article" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro a $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="a" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro img $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag
    $tag="img"
    $model=$model
    $field=$field?ends_with(":")?then("${$field}src", $field)
    $index=$index
    $label=$label
    $attributes=mergedAttributes
    renderEmpty=renderEmpty
    defaultValue=defaultValue
  />
</#macro>

<#macro header $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="header" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro footer $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="footer" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro div $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="div" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro section $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
    <@tag $tag="section" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro span $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="span" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro h1 $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h1" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro h2 $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h2" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro h3 $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h3" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro h4 $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h4" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro h5 $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h5" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro h6 $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="h6" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro ul $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="ul" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro html $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="html" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro body $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="body" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro head $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="head" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro meta $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
    <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
    <@tag $tag="meta" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro title $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attrs)>
  <@tag $tag="title" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro p $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="p" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro ul $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="ul" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro ol $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="ol" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro li $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="li" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro iframe $model=contentModel $field="" $index="" $label="" $attributes={} $omitEventCaptureOverlay=false $eventCaptureOverlayProps={} attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <#assign output>
    <@tag
      $tag="iframe"
      $model=$model
      $field=$field
      $index=$index
      $label=$label
      $attributes=mergedAttributes
    />
  </#assign>
  <#if $omitEventCaptureOverlay>
    ${output}
  <#else>
    <#local eventCaptureOverlayAttributes = {} />
    <#list $eventCaptureOverlayProps as attr, value>
      <#if attr != '$tag' && attr != '$onlyInPreview' && attr != '$attributes'>
        <#local eventCaptureOverlayAttributes += { attr: value } />
      <#elseif attr == '$attributes'>
        <#list value as _attr, _value>
          <#local eventCaptureOverlayAttributes += { _attr: _value } />
        </#list>
      </#if>
    </#list>
    <@eventCaptureOverlay
      $tag=($eventCaptureOverlayProps['$tag']!'div')
      $onlyInPreview=($eventCaptureOverlayProps['$onlyInPreview']!false)
      $attributes=eventCaptureOverlayAttributes
    >
      ${output}
    </@eventCaptureOverlay>
  </#if>
</#macro>

<#macro em $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="em" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro strong $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="strong" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro b $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="b" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro i $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="i" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro small $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="small" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro th $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="th" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro caption $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="caption" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro tr $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="tr" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro td $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="td" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro table $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
    <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
    <@tag $tag="table" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro abbr $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="abbr" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro address $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="address" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro aside $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="aside" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro audio $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="audio" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro video $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="video" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro blockquote $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="blockquote" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro cite $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="cite" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro code $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="code" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro nav $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="nav" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro figure $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="figure" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro figcaption $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="figcaption" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro pre $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="pre" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro time $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="time" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro map $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="map" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro picture $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="picture" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
</#macro>

<#macro source $model=contentModel $field="" $index="" $label="" $attributes={} renderEmpty=true defaultValue="" attrs...>
  <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
  <@tag $tag="source" $model=$model $field=$field $index=$index $label=$label $attributes=mergedAttributes renderEmpty=renderEmpty defaultValue=defaultValue><#nested></@tag>
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
<#---->$collection=$model[$field]!{}
<#---->$containerAttributes={}
<#---->$itemAttributes={}
<#---->$nthItemAttributes={}
>
  <#local $containerAttributes += { 'data-craftercms-type': 'collection' } />
  <#if isEmptyCollection($collection)>
      <#local $containerAttributes += { "class": "${emptyCollectionClass($collection)} ${$containerAttributes['class']!''}" } />
  </#if>
  <#-- Field container element -->
  <@tag
    $tag=$containerTag
    $field=cleanDotNotationString("${$fieldCarryover}.${$field}")
    $index=$indexCarryover
    $model=$model
    $attributes=$containerAttributes
  >
    <#-- When the element isn't on the xml, it comes as type "Hash" -->
    <#if $collection!?is_sequence>
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
    </#if>
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
<#---->$collection=$model[$field]!{}
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
<#---->$collection=$model[$field]!{}
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

<#macro eventCaptureOverlay $onlyInPreview=false $tag="div" $attributes={} attrs...>
  <#assign nested><#nested/></#assign>
  <#if (!$onlyInPreview) || ($onlyInPreview && modePreview)>
    <#assign mergedAttributes = mergeAttributes(attrs, $attributes)>
    <${$tag}
      <#list mergedAttributes as attr, value>
      ${attr}="${value}"
      </#list>
      <#if modePreview>data-craftercms-event-capture-overlay</#if>
    >
      ${nested}
    </${$tag}>
  <#else>
    ${nested}
  </#if>
</#macro>

<#macro ifNotEmpty target="">
  <#if (target?is_string && notEmptyString(target)) || (target?is_sequence && target.item!?size > 0) || (target?is_boolean && target)>
    <#nested>
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

<#function emptyCollectionClass collection>
  <#return shouldAddEmptyStyles(collection)?then('craftercms-empty-collection', '')>
</#function>

<#function emptyFieldClass fieldValue>
  <#return (!fieldValue?has_content)?then('craftercms-empty-field', '')>
</#function>

<#function printIfPreview output>
  <#return modePreview?then(output, '')>
</#function>

<#function printIfNotPreview output>
  <#return (!modePreview)?then(output, '')>
</#function>

<#function printIfNotEmpty target output fallback="">
  <#return notEmptyString(target)?then(output, fallback)>
</#function>

<#function useIfNotEmpty target fallback="">
  <#return notEmptyString(target)?then(target, fallback)>
</#function>

<#function notEmptyString string>
  <#return string?string?trim?has_content>
</#function>

<#function isEmptyString string>
  <#return !notEmptyString(string)>
</#function>
