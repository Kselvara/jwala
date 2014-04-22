var OldTocDataTable = React.createClass({
    getInitialState: function() {
        return {data: {applicationResponseContent:[]}};
    },
    refresh: function() {
        $.ajax({
            url: this.props.url + "?all",
            dataType: "json",
            cache: false,
            success: function(data) {
                this.setState({data: data});
            }.bind(this),
            error: function(xhr, status, err) {

            }.bind(this)
        });
    },
    componentWillMount: function() {
        this.refresh();
    },
    componentDidMount: function() {
        var self = this;
        
        // build column definitions based on props
        var aoColumnDefs  = [];
        var aaSorting     = [];
        var props = this.props;
        
        $(this.props.headerExt).each(function(itemIndex, item, itemArray) {
          aoColumnDefs[itemIndex] = {
            "sTitle": item.sTitle,
            "mData": item.mData,
            "aTargets": [ itemIndex ]};
            
          if(item.bVisible !== undefined) {
            aoColumnDefs[itemIndex].bVisible = item.bVisible;
          }
          
          if(item.tocType == 'link') { 
            aoColumnDefs[itemIndex].mRender = function (data, type, full) {
              if(self.isMounted()) {
                var id = self.props.tableId + "link" + data + full.id.id;
                return React.renderComponentToString(new Link({id:id,
                                                               valId:full.id.id,
                                                               value:data,
                                                               editDialog:props.editDialog,
                                                               thisGrid:self,
                                                               jsonFormDataTransformerCallback:props.jsonFormDataTransformerCallback
                       }));
              } else { return ""; }
            };
          } else if (item.tocType === "array") {
                aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                    var str = "";
                    /* would be better with _Underscore.js : */
                    for (var idx = 0; idx < data.length; idx=idx+1) {
                        str = str + (str === "" ? "" : ", ") + data[idx][item.displayProperty];
                    }
                    return str;
                }
          }

          aaSorting[itemIndex] = [itemIndex, 'asc'];
        });
        
        this.dataTable = 
          $(self.getDOMNode().children[1]).dataTable({
            "aaSorting": aaSorting,
            "aoColumnDefs": aoColumnDefs,
            "bJQueryUI": true,
            "aLengthMenu": [[25, 50, 100, 200, -1],
                            [25, 50, 100, 200, "All"]],
            "iDisplayLength": 25,
            "fnDrawCallback": function(){

                dataTable = this;
                $(dataTable).find("tr").off("click").on("click", function(e) {
                    if ($(this).hasClass("row_selected") ) {
                        $(this).removeClass("row_selected");
                    } else {
                        $(dataTable).find("tr").removeClass("row_selected");
                        $(this).addClass("row_selected");
                    }
                });

            }
        });

    },
    render: function() {
      
        // Table must be in a DIV so that css will work with a table as container
        var table = React.DOM.table({className:"tocDataTable-" + this.props.theme});
        
        var div = React.DOM.div({className:"tocDataTable-" + this.props.theme},
                             this.props.editDialog,
                             table
                             );
                             
        if(this.dataTable !== undefined) {
          this.dataTable.fnClearTable(this.state.data.applicationResponseContent);
          this.dataTable.fnAddData(this.state.data.applicationResponseContent);
          this.dataTable.fnDraw();
        }
        return div;        
    }
});

var Link = React.createClass({
    render: function() {
        // TODO: Remove inline style
        var linkStyle = {"text-decoration":"underline", "background":"none", "color":"blue"};

        $("#" + this.props.id).off("click");
        $("#" + this.props.id).click(this.linkClick);
        return React.DOM.a({id:this.props.id, href:"#", style:linkStyle}, this.props.value);
    },
    linkClick: function(e) {
        var editDialog = this.props.editDialog;
        var thisGrid = this.props.thisGrid;
        var jsonFormDataTransformerCallback = this.props.jsonFormDataTransformerCallback;
        $.getJSON("v1.0/jvms/" + this.props.valId,
            function(data) {
                editDialog.show(jsonFormDataTransformerCallback(data), function(){
                    thisGrid.refresh();
                });

         });
        // next 3 lines stop the browser navigating
        e.preventDefault();
        e.stopPropagation();
        return false; 
    }
});
