package com.my.bookstore.dto;

public class Constants {

    public static final String CLASS_PACKAGE = "com.my.bookstore.dto";

    public static final String AGE_GROUP_TYPE = "com.my.bookstore.model" + ".enums." + AgeGroup.ENUM_NAME;
    public static final String LANGUAGE_TYPE = "com.my.bookstore.model" + ".enums." + Language.ENUM_NAME;
    public static final String BOOK_DTO_TYPE = CLASS_PACKAGE + "." + BookDTO.class.getSimpleName();
    public static final String BOOK_ITEM_DTO_TYPE = CLASS_PACKAGE + "." + BookItemDTO.class.getSimpleName();
    public static final String CLIENT_DTO_TYPE = CLASS_PACKAGE + "." + ClientDTO.class.getSimpleName();
    public static final String EMPLOYEE_DTO_TYPE = CLASS_PACKAGE + "." + EmployeeDTO.class.getSimpleName();
    public static final String ORDER_DTO_TYPE = CLASS_PACKAGE + "." + OrderDTO.class.getSimpleName();

    public static final String INT_TYPE = "java.lang.Integer";
    public static final String LONG_TYPE = "java.lang.Long";
    public static final String STRING_TYPE = "java.lang.String";
    public static final String BIG_DECIMAL_TYPE = "java.math.BigDecimal";
    public static final String LOCAL_DATE_TYPE = "java.time.LocalDate";
    public static final String LOCAL_DATE_TIME_TYPE = "java.time.LocalDateTime";

    static class AgeGroup {
        public static final String ENUM_NAME = "AgeGroup";
    }

    static class Language {
        public static final String ENUM_NAME = "Language";
    }

    static class BookDTO {
        public static final String CLASS_NAME = "BookDTO";
        public static final int CLASS_COUNT_FIELDS = 11;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 11;
    }

    static class BookItemDTO {
        public static final String CLASS_NAME = "BookItemDTO";
        public static final int CLASS_COUNT_FIELDS = 2;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 2;
    }

    static class ClientDTO {
        public static final String CLASS_NAME = "ClientDTO";
        public static final int CLASS_COUNT_FIELDS = 5;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 5;
    }

    static class EmployeeDTO {
        public static final String CLASS_NAME = "EmployeeDTO";
        public static final int CLASS_COUNT_FIELDS = 6;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 6;
    }

    static class OrderDTO {
        public static final String CLASS_NAME = "OrderDTO";
        public static final int CLASS_COUNT_FIELDS = 6;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 6;
    }
}
