package net.example.raccoonclient;

public class Forum {

    int _number;
    String _name = "x";
    int _lastnote;
    
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
        }
    }
}
