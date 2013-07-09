<#function getNavItemName item>
    <#return item.getChildItem("index.xml").navLabel!(item.storeName?replace("-", " ")?replace(".xml", "")?cap_first)>
</#function>

<#function getNavItemUrl item>
    <#return urlTransformationService.transform('storeUrlToRenderUrl', item.storeUrl)>
</#function>