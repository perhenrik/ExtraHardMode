package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.service.config.ConfigNode;

public enum MockConfigNode implements ConfigNode
{
    BOOL_FALSE      ("test01", VarType.BOOLEAN, false),
    BOOL_TRUE       ("test04", VarType.BOOLEAN, true),
    INT_0           ("test02", VarType.INTEGER, 0),
    INT_9           ("test03", VarType.INTEGER, 9),
    STR_0           ("test05", VarType.STRING, "derp"),

    NOTFOUND_BOOL   ("test06", VarType.BOOLEAN, true),
    NOTFOUND_INT    ("test08", VarType.INTEGER, 1),
    NOTFOUND_DOUBLE ("test10", VarType.DOUBLE, 0.0),
    NOTFOUND_STR    ("test07", VarType.STRING, "NaN"),
    NOTFOUND_LIST   ("test09", VarType.LIST, null),

    INHERITS_BOOL   ("test11", VarType.BOOLEAN, true),
    INHERITS_INT    ("test12", VarType.INTEGER, 42),
    INHERITS_DOUBLE ("test13", VarType.DOUBLE, 1.0),
    INHERITS_STR    ("test14", VarType.STRING, "test"),
    INHERITS_LIST   ("test15", VarType.LIST, null),

    /**
     * Integer Validation
     */
    INT_PERC_1      ("per01", VarType.INTEGER, SubType.PERCENTAGE, 100),

    INT_NN_1        ("nn01", VarType.INTEGER, SubType.NATURAL_NUMBER, 42),

    INT_Y_1         ("y01", VarType.INTEGER, SubType.Y_VALUE, 1),

    INT_HP_1        ("hp01", VarType.INTEGER, SubType.HEALTH, 5),
    ;

    String path;
    VarType type;
    SubType subType = null; //initialize because this is optional
    Object val;

    private MockConfigNode (String path, VarType type, Object defaultValue)
    {
        this.path = path;
        this.type = type;
        this.val = defaultValue;
    }

    private MockConfigNode (String path, VarType type, SubType subType, Object defaultValue)
    {
        this(path, type, defaultValue);
        this.subType = subType;
    }

    @Override
    public String getPath ()
    {
        return path;
    }

    @Override
    public VarType getVarType ()
    {
        return type;
    }

    @Override
    public SubType getSubType ()
    {
        return subType;
    }

    @Override
    public Object getDefaultValue ()
    {
        return val;
    }
}
