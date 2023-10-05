<#macro renderNavItem navItem>
    <li <#if navItem.active>class="active"</#if>><a href="${navItem.url}"><#outputFormat "HTML">${navItem.label}</#outputFormat></a></li>
</#macro>

<#macro renderRootItem navItem>
    <@renderNavItem navItem/>
</#macro>

<#macro renderNavItemWithSubItems navItem>
    <li <#if navItem.active>class="dropdown active"<#else>class="dropdown"</#if>>
        <a class="dropdown-toggle" data-toggle="dropdown" href="${navItem.url}"><#outputFormat "HTML">${navItem.label}</#outputFormat></a>
        <ul class="dropdown-menu">
            <#nested>
        </ul>
    </li>
</#macro>

<#macro renderNavSubItem navItem>
    <@renderNavItem navItem/>
</#macro>

<#macro renderNavSubItemWithSubItems navItem>
    <li class="dropdown-submenu">
        <a href="${navItem.url}"><#outputFormat "HTML">${navItem.label}</#outputFormat></a>
        <ul class="dropdown-menu">
            <#nested>
        </ul>
    </li>
</#macro>
