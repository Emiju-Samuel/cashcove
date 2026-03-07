export const baseUrl = "http://localhost:8080/api/v1.0";

const CLOUDINARY_NAME = "dktaopmnb";

export const apiEndpoints = {
    LOGIN: "/login",
    REGISTER: "/register",
    GET_USER_INFO: "/profile",
    GET_ALL_CATEGORIES: "/categories",
    ADD_CATEGORY: "/categories",
    UPDATE_CATEGORY: (categoryId) => `/categories/${categoryId}`,
    DELETE_CATEGORY: (categoryId) => `/categories/${categoryId}`,
    GET_ALL_INCOMES: "/incomes",
    GET_ALL_EXPENSES: "/expenses",
    CATEGORY_BY_TYPE: (type) => `/categories/${type}`,
    ADD_INCOME: "/incomes",
    ADD_EXPENSE: "/expenses",
    DELETE_INCOME: (incomeId) => `/incomes/${incomeId}`,
    DELETE_EXPENSE: (expenseId) => `/expenses/${expenseId}`,
    INCOME_EXCEL_DOWNLOAD: "excel/download/income",
    EXPENSE_EXCEL_DOWNLOAD: "/excel/download/expense",
    EMAIL_INCOME:"/email/income-excel",
    EMAIL_EXPENSE: "/email/expense-excel",
    APPLY_FILTER: "/filter",
    DASHBOARD_DATA: "/dashboard",
    GET_ALL_SUBSCRIPTIONS: "/subscriptions",
    ADD_SUBSCRIPTION: "/subscriptions",
    UPDATE_SUBSCRIPTION: (subscriptionId) => `/subscriptions/${subscriptionId}`,
    DELETE_SUBSCRIPTION: (subscriptionId) => `/subscriptions/${subscriptionId}`,
    UPLOAD_IMAGE: `https://api.cloudinary.com/v1_1/${CLOUDINARY_NAME}/image/upload`,
}