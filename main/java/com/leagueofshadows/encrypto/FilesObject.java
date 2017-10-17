package com.leagueofshadows.encrypto;


class FilesObject {
    private String name;
    private int download;
    private String address;
    private int sent;
    private String localId;
    private String databaseId;
    private String from;
    private int decrypt;
    private String key;
    FilesObject(String name,String address,int download,int sent,String localId,String databaseId,String from,int decrypt,String key)
    {
        this.from = from;
        this.address=address;
        this.name=name;
        this.sent=sent;
        this.download= download;
        this.databaseId = databaseId;
        this.localId = localId;
        this.decrypt = decrypt;
        this.key = key;
    }

    String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    int getSent()
    {
        return sent;
    }

    String getDatabaseId() {
        return databaseId;
    }

    String getLocalId() {
        return localId;
    }

    public String getFrom() {
        return from;
    }

    int getDownload() {
        return download;
    }

    int getDecrypt() {
        return decrypt;
    }

    String getKey() {
        return key;
    }

    void setDownload() {
        this.download=1;
    }
}
