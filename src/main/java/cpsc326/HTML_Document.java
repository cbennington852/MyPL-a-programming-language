package cpsc326;

public class HTML_Document {
    public final static String start = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        #myInput {
                            background-position: 10px 10px;
                            background-repeat: no-repeat;
                            width: 100%;
                            font-size: 16px;
                            padding: 12px 20px 12px 40px;
                            border: 1px solid #ddd;
                            margin-bottom: 12px;
                        }

                        .table {
                            border-collapse: collapse;
                            width: 100%;
                            border: 1px solid #ddd;
                            font-size: 18px;
                        }

                        .table th, .table td {
                            text-align: left;
                            padding: 12px;
                        }

                        .table tr {
                            border-bottom: 1px solid #ddd;
                        }

                        .table tr.header {
                            background-color: #f1f1f1;
                        }

                        .structDefInputs {
                            padding-left: 20px;
                        }

                        .structDef {

                        }
                        .funDef {

                        }

                    </style>
                </head>
                <body>

                <h2>MyPl JavaDoc</h2>

                <input type="text" id="myInput" onkeyup="myFunction()" placeholder="Search for .." title="Type in a name">

                <table class="table" id="myTable">
                    <tr class="header">
                        <th style="width:60%;"><h2>Structs</h2></th>
                    </tr>
                """;
    
    public final static String middle= """
                </table>
                <p></p>

                <table class="table" id="myTable2">
                    <tr class="header">
                        <th style="width:60%;"><h2>Functions</h2></th>
                    </tr>
                """;
    
    
    public final static String end = """
                </table>

                <script>
                    function myFunction() {
                        var input, filter, table, tr, td, i, txtValue;
                        input = document.getElementById("myInput");
                        filter = input.value.toUpperCase();
                        table = document.getElementById("myTable");
                        tr = table.getElementsByTagName("tr");
                        for (i = 0; i < tr.length; i++) {
                            td = tr[i].getElementsByTagName("td")[0];
                            if (td) {
                                txtValue = td.textContent || td.innerText;
                                if (txtValue.toUpperCase().indexOf(filter) > -1) {
                                    tr[i].style.display = "";
                                } else {
                                    tr[i].style.display = "none";
                                }
                            }
                        }

                        table = document.getElementById("myTable2");
                        tr = table.getElementsByTagName("tr");
                        for (i = 0; i < tr.length; i++) {
                            td = tr[i].getElementsByTagName("td")[0];
                            if (td) {
                                txtValue = td.textContent || td.innerText;
                                if (txtValue.toUpperCase().indexOf(filter) > -1) {
                                    tr[i].style.display = "";
                                } else {
                                    tr[i].style.display = "none";
                                }
                            }
                        }
                    }
                </script>

                </body>
                </html>

                """;
}
