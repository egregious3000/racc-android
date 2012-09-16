package net.example.raccoonclient;

public class Forum {

    int _number;
    String _name = "x";
    int _lastnote;
    String _flags = "";
    int _todo;
    
//    topic:180       name:Stereo And Electronic Technology   lastnote:64104  flags:nosubject,sparse  admin:acct579504-oldisca/Copper Lethe/(hidden)  todo:1
//    topic:0   name:Lobby  lastnote:2379   flags:nosubject,sparse  admin:acct578247-oldisca/(Unknown ISCABBS User)/(hidden)
    Forum(String s) {
        String[] fields = s.split("\t");
        for (String f : fields) {
            String[] k = f.split(":");
            if (k[0].equals("topic"))
                _number = Integer.parseInt(k[1]);
            if (k[0].equals("name"))
                _name = k[1];
            if (k[0].equals("lastnote"))
                _lastnote = Integer.parseInt(k[1]);
            if (k[0].equals("flags"))
                _flags = k[1];
            if (k[0].equals("todo"))
                _todo = Integer.parseInt(k[1]);
        }
    }
    public String getHeader() {
        if (_todo == 0)
            return _name;
        else
            return (_name + " (" + _todo + ")"); 
    }
    
}
