
<#macro toolSupport>
  <#if modePreview>
    <script src="/studio/static-assets/libs/requirejs/require.js" data-main="/studio/overlayhook?site=NOTUSED&page=NOTUSED&cs.js"></script>
  </#if>
</#macro>

<#macro cstudioOverlaySupport>
  <@toolSupport />
</#macro>

<#macro componentAttr path="" ice=false iceGroup="">
  <#if modePreview>data-studio-component-path="${path}" data-studio-component="${path}"
    <#if ice==true>
      <@iceAttrLegacy path=path iceGroup=iceGroup/>
    </#if>
  </#if>
</#macro>

<#macro componentContainerAttr target objectId="">
  <#if modePreview> data-studio-components-target="${target}" data-studio-components-objectId="${objectId}"</#if>
</#macro>

<#-- Main macro for ICE attributes -->
<#macro iceAttr iceGroup="" path="" label="" component={} >
  <#if !modePreview>
    <#return>
  </#if>
  <#if component?has_content >
    <@iceAttrComponent iceGroup=iceGroup label=label component=component />
  <#else>
    <@iceAttrLegacy iceGroup=iceGroup label=label path=path />
  </#if>
</#macro>

<#-- Macro to handle ICE attributes for a SiteItem -->
<#macro iceAttrComponent iceGroup="" label="" component={} >
  <#-- Figure out the label to use -->
  <#if label?has_content >
    <#assign actualLabel = label />
  <#elseif iceGroup?has_content >
    <#assign actualLabel = iceGroup />
  <#else>
    <#assign actualLabel = component["internal-name"] />
  </#if>
  data-studio-ice="${iceGroup}" data-studio-ice-label="${actualLabel}" data-studio-ice-path="${component.storeUrl}"
  <#-- If the given component has a parent -->
  <#if !component.getDom()?has_content >
    data-studio-embedded-item-id="${component.objectId}"
  </#if>
</#macro>

<#-- Macro to handle ICE attributes for a path -->
<#macro iceAttrLegacy iceGroup="" label="" path="" >
  <#if label?has_content >
    <#assign actualLabel = label />
  <#elseif iceGroup?has_content >
    <#assign actualLabel = iceGroup />
  <#else>
    <#assign actualLabel = path />
  </#if>
  data-studio-ice="${iceGroup}" data-studio-ice-label="${actualLabel}"
  <#if path?has_content >
    data-studio-ice-path="${path}"
  </#if>
</#macro>

<#macro ice id="" component="" componentPath="">
  <#if modePreview>
    <div data-studio-ice="${id}" ></div>
  </#if>
</#macro>


<#macro draggableComponent id="" component="" componentPath="">
  <#if modePreview>
    <#if id != "" && component == "" && componentPath == "">
      <@ice id=id>
        <div id='${id}' class='cstudio-draggable-component'><#nested></div>
      </@ice>
    <#elseif id == "" && componentPath == "">
      <@ice component=component>
        <div id="cstudio-component-${component.key}" class='cstudio-draggable-component'><#nested></div>
      </@ice>
    <#elseif id == "" && component == "">
      <@ice componentPath=componentPath>
        <div id="cstudio-component-${componentPath}" class='cstudio-draggable-component'><#nested></div>
      </@ice>
    </#if>
  <#else>
    <#nested>
  </#if>
</#macro>

<#macro componentZone id="">
  <div class="cstudio-component-zone" id="zone-${id}">
    <@ice id=id>
      <#nested>
    </@ice>
  </div>
</#macro>
