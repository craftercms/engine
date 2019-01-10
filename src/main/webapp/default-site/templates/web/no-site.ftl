<!--
  ~ Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -->
<!doctype html>
<!-- paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->
<!--[if lt IE 7]> <html class="no-js ie6" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js ie7" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js ie8" lang="en"> <![endif]-->
<!-- Consider adding an manifest.appcache: h5bp.com/d/Offline -->
<!--[if gt IE 8]><!--> <html class="no-js" lang="en"> <!--<![endif]-->
<head>
  <meta charset="utf-8">

  <!-- Use the .htaccess and remove these lines to avoid edge case issues.
       More info: h5bp.com/b/378 -->
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  
  <title>Crafter CMS: NO SITE SET</title>
  <meta name="description" content="">
  <meta name="author" content="">

  <!-- Mobile viewport optimized: j.mp/bplateviewport -->
  <meta name="viewport" content="width=device-width,initial-scale=1">

  <!-- Place favicon.ico and apple-touch-icon.png in the root directory: mathiasbynens.be/notes/touch-icons -->

  <!-- CSS: implied media=all -->
  <!-- CSS concatenated and minified via ant build script-->
  <link href='${urlTransformationService.transform('toWebAppRelativeUrl', '/static-assets/css/default-style.css')}' rel='stylesheet' type='text/css'>
  <link href='http://fonts.googleapis.com/css?family=Pontano+Sans' rel='stylesheet' type='text/css'>
  <!-- end CSS-->

  <!-- More ideas for your <head> here: h5bp.com/d/head-Tips -->

  <!-- All JavaScript at the bottom, except for Modernizr / Respond.
       Modernizr enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width CSS3 Media Queries
       For optimal performance, use a custom Modernizr build: www.modernizr.com/download/ -->
  <script src="js/libs/modernizr-2.0.6.min.js"></script>
</head>

<body>
  <div id="container">
    <div id="side-col">
    <header>
      <a class="logo" href="#"><img src="/static-assets/img/crafter-logo-transparent.png" alt="Crafter CMS by Crafter Software"   /></a>
      <nav>
        <ul>
          <li class="active"><span>Crafter CMS</span></li>
          <li><a href="/studio"><span>Crafter Studio</span></a></li>
          <li><a href="http://www.craftersoftware.com/products"><span>Overview &amp; Features</span></a></li>
          <li><a href="http://www.craftercms.org/downloads"><span>Download</span></a></li>
          <li><a href="http://www.craftersoftware.com/resources"><span>Tutorials &amp; Screencasts</span></a></li>
          <li><a href="http://docs.craftercms.org"><span>Documentation</span></a></li>
          <li><a href="http://www.craftersoftware.com/about/partners"><span>Professional Services</span></a></li>
        </ul>
      </nav>
    </header>
    <aside>



    </aside>
  </div>    
    <div id="main" role="main">
    
    <section class="intro bgOpaque">
      <h1>Welcome to Crafter CMS</h1>
      <p>Crafter CMS is an award-winning open source CMS built from the ground up as a modern platform for crafting high performance digital experiences through targeted delivery of personalized content.</p>
    </section>

    <section class="features bgOpaque">
        <h1>No Site is Set!</h1>
        <p>Crafter Engine has no sites configured for this domain.</p>
    </section>
      

      <nav>
      <ul>
        <!--li class="active">Overivew &amp; Features</li>
        <li><a href="#">Tutorials &amp; Screencasts</a></li>
        <li><a href="#">FAQs</a></li>
        <li><a href="#">Our Contributors</a></li-->
      </ul>
    </nav>
    </div>
    
  </div> <!--! end of #container -->
  <footer>
    <p>Copyright &copy; 2007 - ${.now?string('yyyy')}, Crafter Software Corporation. All rights reserved.<br />
    Crafter CMS is free and open source software licensed under the GNU General Public License (GPL) version 3.0  
    </p>
  </footer>

  <!-- Grab Google CDN's jQuery, with a protocol relative URL; fall back to local if offline -->
  <script src="/static-assets/js/libs/jquery-1.6.2.min.js"></script>


  <!-- scripts concatenated and minified via ant build script-->
  <script defer src="/static-assets/js/plugins.js"></script>
  <script defer src="/static-assets/js/script.js"></script>
  <!-- end scripts-->
  
</body>
</html>