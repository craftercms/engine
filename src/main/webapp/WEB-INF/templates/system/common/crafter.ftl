<#include "/templates/system/common/ice.ftl"/>
<#include "/templates/system/plugins/definitions.ftl" ignore_missing=true />

<#macro head>
    <#include "/templates/system/plugins/head.ftl" ignore_missing=true />
</#macro>

<#macro body_top>
    <#include "/templates/system/plugins/body_top.ftl" ignore_missing=true />
</#macro>

<#macro body_bottom initializePageBuilder=true pageBuilderProps="{}">
    <#include "/templates/system/plugins/body_bottom.ftl" ignore_missing=true />
    <#if initializePageBuilder>
        <@initPageBuilder props=pageBuilderProps />
    </#if>
</#macro>

<#macro renderNavItem navItem>
    <#assign storeUrl = urlTransformationService.transform('renderUrlToStoreUrl', navItem.url)>
    <#assign item = siteItemService.getSiteItem(storeUrl) />
    <li <#if navItem.active>class="active"</#if>>
        <@a $model=item href="${navItem.url}">
            ${navItem.label}
        </@a>
    </li>
</#macro>

<#macro renderRootItem navItem>
    <@renderNavItem navItem/>
</#macro>

<#macro renderNavItemWithSubItems navItem>
    <#assign storeUrl = urlTransformationService.transform('renderUrlToStoreUrl', navItem.url)>
    <#assign item = siteItemService.getSiteItem(storeUrl) />
    <li <#if navItem.active>class="dropdown active"<#else>class="dropdown"</#if>>
        <@a class="dropdown-toggle" $model=item href="${navItem.url}" $attrs={'data-toggle': 'dropdown'}>
            ${navItem.label}
        </@a>
        <ul class="dropdown-menu">
            <#nested>
        </ul>
    </li>
</#macro>

<#macro renderNavSubItem navItem>
    <@renderNavItem navItem/>
</#macro>

<#macro renderNavSubItemWithSubItems navItem>
    <#assign storeUrl = urlTransformationService.transform('renderUrlToStoreUrl', navItem.url)>
    <#assign item = siteItemService.getSiteItem(storeUrl) />
    <li class="dropdown-submenu">
        <@a $model=item href="${navItem.url}">
            ${navItem.label}
        </@a>
        <ul class="dropdown-menu">
            <#nested>
        </ul>
    </li>
</#macro>

<#macro renderNavigation url depth includeRoot = false>
    <#assign navTree = navTreeBuilder.getNavTree(url, depth, Request.pageUrl)/>

    <#if includeRoot>
        <@renderRootItem navTree/>
    </#if>

    <#assign subItems = navTree.subItems/>
    <#if (subItems?size > 0)>
        <@renderNavigationItems subItems 1/>
    </#if>
</#macro>

<#macro renderNavigationItems navItems currDepth>
    <#list navItems as navItem>
        <#assign subItems = navItem.subItems/>
        <#if currDepth == 1>
            <#if (subItems?size > 0)>
                <@renderNavItemWithSubItems navItem>
                    <@renderNavigationItems subItems, currDepth + 1/>
                </@renderNavItemWithSubItems>
            <#else>
                <@renderNavItem navItem/>
            </#if>
        <#else>
            <#if (subItems?size > 0)>
                <@renderNavSubItemWithSubItems navItem>
                    <@renderNavigationItems subItems, currDepth + 1/>
                </@renderNavSubItemWithSubItems>
            <#else>
                <@renderNavSubItem navItem/>
            </#if>
        </#if>
    </#list>
</#macro>