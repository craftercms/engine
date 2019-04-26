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
	<head>
		<meta charset="utf-8">
		<title>Crafter CMS: No Site Set</title>

		<!-- start CSS -->
		<link href='${urlTransformationService.transform('toWebAppRelativeUrl', '/static-assets/css/default-style.css')}' rel='stylesheet' type='text/css'>
		<link href='http://fonts.googleapis.com/css?family=Pontano+Sans' rel='stylesheet' type='text/css'>
		<!-- end CSS-->

		<!-- All JavaScript at the bottom, except for Modernizr / Respond.
		Modernizr enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width CSS3 Media Queries
		For optimal performance, use a custom Modernizr build: www.modernizr.com/download/ -->
		<script src="js/libs/modernizr-2.0.6.min.js"></script>

		<!-- Favicon Setting -->
		<link rel="icon" type="image/x-icon" href='${urlTransformationService.transform('toWebAppRelativeUrl', '/static-assets/img/favicon.ico')}'/>
	</head>

	<body>
		<div id="container">
			<div id="side-col">
				<header>
					<a class="logo" href="#"><img src="/static-assets/img/logo.svg" alt="Crafter CMS"/></a>
					<nav>
						<ul>
							<li class="active"><span>Crafter CMS</span></li>
							<li><a href="/studio"><span>Back to Crafter Studio</span></a></li>
							<li><a href="http://docs.craftercms.org"><span>Documentation</span></a></li>
							<li><a href="http://www.craftersoftware.com/resources"><span>Tutorials &amp; Screencasts</span></a></li>
							<li><a href="http://www.craftersoftware.com/about/partners"><span>Professional Services</span></a></li>
						</ul>
					</nav>
				</header>
			</div>

			<div id="main" role="main">
				<section class="features bgOpaque">
					<h1>Welcome to Crafter CMS</h1>
					<h2>Error: No Site is Set.</h2>
					<p>Crafter Engine has no sites configured for this domain.</p>
					<h2>What is Crafter CMS?</h2>
					<p>Crafter CMS is a modern content management platform for building digital experience applications using:</p>
						<ul>
							<li>SPA frameworks like React, Vue, and Angular</li>
							<li>AR/VR applications using A-Frame</li>
							<li>Native Mobile and Headless applications</li>
							<li>HTML5 Websites using Bootstrap or other HTML frameworks</li>
						</ul>
					<p>Crafter is a dynamic CMS based on Git and supports DevOps processes for code and content. It is also a hybrid-headless, API-first (GraphQL, REST, in-process) CMS, and that allows developers to use their favorite UI frameworks and tools.</p>
					<p>Crafter CMS differentiates itself from existing CMSs with its architecture: microservices-based, serverless, elastic and planet-wide scalability. To learn more, see the <a href="http://docs.craftercms.org">docs</a>.</p>
				</section>
			</div>
		</div>
		<footer>
			<p>Copyright &copy; 2007 - ${.now?string('yyyy')}, Crafter Software Corporation. All rights reserved.<br />
			Crafter CMS is open source software licensed under the GNU General Public License (GPL) version 3.0.</p>
		</footer>

		<!-- Grab Google CDN's jQuery, with a protocol relative URL; fall back to local if offline -->
		<script src="/static-assets/js/libs/jquery-1.6.2.min.js"></script>

		<!-- scripts concatenated and minified via ant build script-->
		<script defer src="/static-assets/js/plugins.js"></script>
		<script defer src="/static-assets/js/script.js"></script>
		<!-- end scripts-->
	</body>
</html>
