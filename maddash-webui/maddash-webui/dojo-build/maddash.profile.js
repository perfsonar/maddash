dependencies ={
    layers:  [
        {
        name: "dojo-maddash.js",
        dependencies: [
            //NOTE: Do NOT include dojo.date.locale, DateTextBox, or TimeTextBox because date formatting breaks
            //direct dependencies
            "dojo._base.connect",
            "dojo.parser",
            "dijit.layout.ContentPane", 
            "dijit.DropDownMenu",
            "dijit.MenuBar",
            "dijit.MenuItem",
            "dijit.MenuSeparator",
            "dijit.PopupMenuBarItem",
            "dijit.form.Slider",
            "dijit.layout.TabContainer", 
            "dijit.TitlePane",
            "dijit.form.Textarea",
            //indirect dependencies 
            "dijit._base",
            "dijit.WidgetSet",
            "dijit._base.focus",
            "dijit._base.place",
            "dijit._base.popup",
            "dijit._base.scroll",
            "dijit._base.sniff",
            "dijit._base.typematic",
            "dijit._base.wai",
            "dijit._base.window",
            "dijit.form.DropDownButton",
            "dijit.form.ComboButton",
            "dojo.fx.Toggler",
            "dijit.CheckedMenuItem",
            "dijit.form.FilteringSelect",
        ]
        }
    ],
    prefixes: [
        [ "dijit", "../dijit" ],
        [ "dojox", "../dojox" ],
    ]
};

