<#macro toolSupport>
	<@initExperienceBuilder props=xbProps />
	<script>
		console.warn('Deprecated macros detected. Please refer to the documentation to migrate to the new macros: https://craftercms.com/docs/4.2/by-role/developer/upgrade/upgrading-in-context-editing.html#upgrading-in-context-editing');
		const deprecatedIceGroupCount = document.querySelectorAll('[data-craftercms-deprecated-ice-group]').length;
		if (deprecatedIceGroupCount > 0) {
			console.warn('\'iceGroup\' parameter is not supported anymore. Please refer to the documentation to migrate to the new macros: https://craftercms.com/docs/4.2/by-role/developer/upgrade/upgrading-in-context-editing.html#upgrading-in-context-editing');
		}
	</script>
</#macro>

<#macro cstudioOverlaySupport>
	<@toolSupport />
</#macro>

<#-- Macro for component attributes - Sets the attribute to register a component under a dropZone
  path: Used to retrieve the item from the siteItemService (if no 'component' is provided)
  component: Model object to use for the component attributes
  ice: Not used
  iceGroup: Not supported
-->
<#macro componentAttr path="" ice=false iceGroup="" component={}>
	<#if !modePreview>
		<#return>
	</#if>
	<#if !component?has_content>
		<#assign item = siteItemService.getSiteItem(path)/>
	<#else>
		<#assign item = component/>
	</#if>
	data-craftercms-model-id="${item.objectId!"__ID_NOT_FOUND__"}"
	data-craftercms-model-path="${item.storeUrl!"__PATH_NOT_FOUND__"}"
</#macro>

<#-- Macro for drop zone attributes - Sets the attributes to enable an element as a drop-zone
  target: the fieldId of the dropZone
  objectId: The objectId of the model
  component: The model object to use for the drop-zone attributes
 -->
<#macro componentContainerAttr target objectId="" component={}>
	<#if !modePreview>
		<#return>
	</#if>
	<#local $model = component!contentModel>
	<#if component?has_content>
		<#local $collection =  component[target]!{}>
	<#else>
		<#local $collection =  contentModel[target]!{}>
	</#if>

	data-craftercms-type="collection"
	data-craftercms-model-path="${$model.storeUrl!"__PATH_NOT_FOUND__"}"
	data-craftercms-model-id="${$model.objectId!"__ID_NOT_FOUND__"}"
	<#if target?has_content>
	<#---->data-craftercms-field-id="${target}"
	</#if>
</#macro>

<#-- Macro for ICE attributes - Set the attributes to enable ICE on an element
  iceGroup: If set, opens the form with the fields set on the group
  path: Used to retrieve the item from the siteItemService (if no 'component' is provided)
  component: Model object to use for the ICE attributes
-->
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
	data-craftercms-deprecated-ice-group="${iceGroup}"
	data-craftercms-model-id="${item.objectId!"__ID_NOT_FOUND__"}"
	data-craftercms-model-path="${item.storeUrl!"__PATH_NOT_FOUND__"}"
</#macro>

<#macro ice id="" component="" componentPath="">
	<script>
		console.warn('Non-working macro \'ice\'. Please refer to the documentation to migrate to the new macros: https://craftercms.com/docs/4.2/by-role/developer/upgrade/upgrading-in-context-editing.html#upgrading-in-context-editing');
	</script>
	<#if modePreview>
		<div data-studio-ice="${id}"></div>
	</#if>
</#macro>

<#macro draggableComponent id="" component="" componentPath="">
	<script>
		console.warn('Non-working macro \'draggableComponent\'. Please refer to the documentation to migrate to the new macros: https://craftercms.com/docs/4.2/by-role/developer/upgrade/upgrading-in-context-editing.html#upgrading-in-context-editing');
	</script>
	<#if modePreview>
		<#if id != "" && component == "" && componentPath == "">
			<@ice id=id>
				<div id='${id}' class='cstudio-draggable-component'><#nested></div>
			</@ice>
		<#elseif id == "" && componentPath == "">
			<@ice component=component>
				<div id="cstudio-component-${component.key}"
				     class='cstudio-draggable-component'><#nested></div>
			</@ice>
		<#elseif id == "" && component == "">
			<@ice componentPath=componentPath>
				<div id="cstudio-component-${componentPath}"
				     class='cstudio-draggable-component'><#nested></div>
			</@ice>
		</#if>
	<#else>
		<#nested>
	</#if>
</#macro>

<#macro componentZone id="">
	<script>
		console.warn('Non-working macro \'componentZone\'. Please refer to the documentation to migrate to the new macros: https://craftercms.com/docs/4.2/by-role/developer/upgrade/upgrading-in-context-editing.html#upgrading-in-context-editing');
	</script>
	<div class="cstudio-component-zone" id="zone-${id}">
		<@ice id=id>
			<#nested>
		</@ice>
	</div>
</#macro>
