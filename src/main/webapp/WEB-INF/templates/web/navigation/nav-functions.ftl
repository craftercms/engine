<#function getNavItemName item>
    <#assign actualNavItem = item>
    <#assign navItemLabel = "">
    <#if item.folder>
        <#assign actualNavItem = item.getChildItem("index.xml")>
    </#if>
    <#if (actualNavItem.navLabel?? && actualNavItem.navLabel?length > 0)>
        <#assign navItemLabel = actualNavItem.navLabel/>
    <#else>
        <#assign navItemLabel = (item.storeName?replace("-", " ")?replace(".xml", "")?cap_first)/>
    </#if>
    <#return navItemLabel>
</#function>

<#function getNavItemUrl item>
    <#return urlTransformationService.transform('storeUrlToRenderUrl', item.storeUrl)>
</#function>