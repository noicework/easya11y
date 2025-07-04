<!DOCTYPE html>
<html>
<head>
    <title>Form Embed</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${ctx.contextPath}/.resources/easya11y/webresources/css/style.css?v=${.now?long}">
    <script src="${ctx.contextPath}/.resources/easya11y/webresources/js/easya11y.js?v=${.now?long}"></script>

    <style type="text/css">
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Roboto', -apple-system, BlinkMacSystemFont, 'Segoe UI', Oxygen, Ubuntu, Cantarell, sans-serif;
            background-color: #f5f5f5;
            color: #333;
            line-height: 1.6;
            padding: 20px;
            font-size: 14px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 24px;
        }

        .easya11y-title {
            color: #2c3e50;
            margin-bottom: 30px;
            font-size: 2.5em;
            border-bottom: 2px solid #068449;
            padding-bottom: 10px;
        }

        .main {
            margin-top: 20px;
        }
    </style>

[@cms.page /]
</head>
<body class="easya11y ${cmsfn.editMode?then('easya11y-author-mode', '')}">
<div class="container">
    <h1 class="easya11y-title">Create your new form</h1>
    
    <div class="main">
    [@cms.area name="main"/]
    </div>
</div>
</body>
</html>
