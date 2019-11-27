
<#macro toolSupport>
  <#if modePreview>
    <script src="/studio/static-assets/libs/requirejs/require.js" data-main="/studio/overlayhook?site=NOTUSED&page=NOTUSED&cs.js"></script>
  </#if>
</#macro>

<#macro cstudioOverlaySupport>
  <@toolSupport />
</#macro>

<#-- Macro for component attributes -->
<#macro componentAttr path="" ice=false iceGroup="" component={}>
  <#if !modePreview>
    <#return>
  </#if>
  <#if !component?has_content>
    <#assign item = siteItemService.getSiteItem(path)/>
  <#else>
    <#assign item = component/>
  </#if>
  data-studio-component="${item.storeUrl}"
  data-studio-component-path="${item.storeUrl}"
  <#if ice>
    <@iceAttr component=item iceGroup=iceGroup/>
  </#if>
  <#if !ice && !item.dom?has_content >
    data-studio-embedded-item-id="${item.objectId}"
  </#if>
</#macro>

<#-- Macro for drop zone attributes -->
<#macro componentContainerAttr target objectId="" component={}>
  <#if !modePreview>
    <#return>
  </#if>
  data-studio-components-target="${target}"
  <#if component?has_content>
    <#-- Use the component object -->
    data-studio-components-objectId="${component.objectId}"
    data-studio-zone-content-type="${component['content-type']}"
  <#else>
    <#-- Use objectId for backwards compatibility -->
    data-studio-components-objectId="${objectId}"
    data-studio-zone-content-type="${contentModel['content-type']}"
  </#if>
</#macro>

<#-- Macro for ICE attributes -->
<#macro iceAttr iceGroup="" path="" label="" component={} >
  <#if !modePreview>
    <#return>
  </#if>
  <#if !(component?has_content)>
    <#if path?has_content>
        <#assign item = siteItemService.getSiteItem(path)/>
    <#else>
        <#assign item = contentModel/>
    </#if>
  <#else>
    <#assign item = component/>
  </#if>
  <#-- Figure out the label to use -->
  <#if label?has_content >
    <#assign actualLabel = label />
  <#elseif iceGroup?has_content >
    <#assign actualLabel = iceGroup />
  <#else>
    <#assign actualLabel = item["internal-name"]!"" />
  </#if>
  data-studio-ice="${iceGroup}" data-studio-ice-label="${actualLabel}" data-studio-ice-path="${item.storeUrl}"
  <#-- If the given component has a parent -->
  <#if !item.dom?has_content >
    data-studio-embedded-item-id="${item.objectId}"
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
