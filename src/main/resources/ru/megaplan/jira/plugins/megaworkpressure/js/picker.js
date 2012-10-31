AJS.$.namespace("GB.gadget.fields");
GB.gadget.fields.picker = function(gadget, userpref, options, label) {
    if(!AJS.$.isArray(options)){
        options = [options];
    }

      return {
        id: "statuspicker_" + userpref,
        userpref: userpref,
        label: label||"Picker",
        description: "Select",
        type: "multiselect",
        selected: gadget.getPref(userpref),
        options: options
      };
};