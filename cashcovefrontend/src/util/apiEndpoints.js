export const baseUrl = "http://localhost:8080/api/v1.0";

const CLOUDINARY_NAME = "dktaopmnb";

export const apiEndpoints = {
    LOGIN: "/login",
    REGISTER: "/register",
    GET_USER_INFO: "/profile",
    GET_ALL_CATEGORIES: "/categories",
    ADD_CATEGORY: "/categories",
    UPDATE_CATEGORY: (categoryId) => `/categories/${categoryId}`,
    GET_ALL_INCOMES: "/incomes",
    CATEGORY_BY_TYPE: (type) => `/categories/${type}`,
    ADD_INCOME: "/incomes",
    DELETE_INCOME: (incomeId) => `/incomes/${incomeId}`,
    INCOME_EXCEL_DOWNLOAD: "excel/download/income",
    EXPENSE_EXCEL_DOWNLOAD: "/excel/download/expense",
    EMAIL_INCOME:"/email/income-excel",
    EMAIL_EXPENSE: "/email/expense-excel",
    APPLY_FILTER: "/filter",
    DASHBOARD_DATA: "/dashboard",
    UPLOAD_IMAGE: `https://api.cloudinary.com/v1_1/${CLOUDINARY_NAME}/image/upload`,
}