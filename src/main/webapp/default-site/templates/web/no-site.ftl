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
	<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600" rel="stylesheet">
	<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap-reboot.min.css"
				rel="stylesheet">
	<style>
		html, body {
			height: 100%;
		}

		body {
			font-family: 'Open Sans', sans-serif;
			background-position: center center;
			background-image: url('${urlTransformationService.transform('toWebAppRelativeUrl', '/static-assets/img/gears.jpg')}');
			background-repeat: no-repeat;
			background-size: cover;
			align-items: center;
			justify-content: center;
			display: flex;
		}

		#main {
			margin: 20px;
			padding: 20px;
			max-width: 400px;
			border-radius: 20px;
			background-color: #fff;
			background-color: rgba(255,255,255,.9);
			position: relative;
		}

		.footer {
			font-size: 65%;
			color: #999;
		}

		.logo {
			width: 200px;
			margin: 20px auto;
			display: block;
		}

		.navigation {
			margin: 0 0 20px;
		}

		.navigation--title {
			font-size: 80%;
		}

		.navigation--link {
			font-size: 80%;
			display: block;
		}

		.footer,
		.no-site-message {
			text-align: center;
		}
	</style>
	<!-- end CSS-->

	<!-- Favicon Setting -->
	<link rel="icon" type="image/x-icon"
				href='${urlTransformationService.transform('toWebAppRelativeUrl', '/static-assets/img/favicon.ico')}'/>
</head>

<body>

<main id="main">

	<a href="https://craftercms.org">
		<img src="${urlTransformationService.transform('toWebAppRelativeUrl', '/static-assets/img/logo.svg')}" alt="Crafter CMS"/>
	</a>

	<div class="no-site-message">
		<p>
			Crafter CMS has no site configured for this domain.
			Please configure the site you want to show or select a site on <a href="/studio">the authoring environment</a>.
		</p>
	</div>

	<nav class="navigation">
		<h2 class="navigation--title">Helpful links:</h2>
		<a class="navigation--link" href="/studio">Authoring environment</a>
		<a class="navigation--link" href="http://docs.craftercms.org">Docs Homepage</a>
		<a class="navigation--link" href="http://www.craftersoftware.com/resources">Tutorials &amp; Screencasts</a>
		<a class="navigation--link" href="http://www.craftersoftware.com/about/partners">Professional Services</a>
	</nav>

	<footer class="footer">
		Copyright &copy; 2007 - ${.now?string('yyyy')}, Crafter Software Corporation. All rights reserved.
		Crafter CMS is open source software licensed under the GNU General Public License (GPL) version 3.0.
	</footer>

</main>

</body>
</html>
