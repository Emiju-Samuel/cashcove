import React, { useEffect, useState } from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'
import { apiEndpoints } from '../util/apiEndpoints';
import axiosConfig from '../util/axiosConfig';
import ExpenseOverview from '../Components/ExpenseOverview';
import ExpenseList from '../Components/ExpenseList';
import AddExpenseForm from '../Components/AddExpenseForm';

import DeleteAlert from '../Components/DeleteAlert';
import { toast } from 'react-toastify';
import Modal from '../Components/Modal';

const Expense = () => {
    UseUser();
  
    const [expenseData, setExpenseData] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
  
    const [openAddExpenseModal, setOpenAddExpenseModal] = useState(false);
    const [openDeleteAlert, setOpenDeleteAlert] = useState({
      show: false,
      data: null,
    })
  
    // Fetch expense details from the API
    const fetchExpenseDetails = async () => {
      if(loading) return;
      setLoading(true);
  
      try{
      const response = await axiosConfig.get(apiEndpoints.GET_ALL_EXPENSES);
      if(response.status === 200){
        setExpenseData(response.data);
      }
    }catch(error){
      console.error("Failed to fetch expense details:", error);
      toast.error(error.response?.data?.message || "failed to fetch expense details");
    }finally{
      setLoading(false);
    }
    }
  
    // fetch categories for expense
    const fetchExpenseCategories = async () => {
      try{
        const response = await axiosConfig.get(apiEndpoints.CATEGORY_BY_TYPE("expense"));
        if(response.status === 200){
          console.log("Expense categories", response.data)
          setCategories(response.data);
        }
      }catch(error){
        console.log("Failed to fetch expense categories: ", error);
        toast.error(error.data?.message || "Failed to fetch expense categories");
      }
    }
  
    // save the expense details
    const handleAddExpense = async (expense) => {
      const {name, amount, date, icon, categoryId} = expense;
  
      // validation
      if(!name.trim()){
        toast.error("Please enter a name");
        return;
      }
  
      if(!amount || isNaN(amount) || Number(amount) <= 0){
        toast.error("Amount should be a valid numebr greater than 0");
        return;
      }
  
      if(!date){
        toast.error("Please select a date");
        return;
      }
  
      const today = new Date().toISOString().split("T")[0];
      if(date > today){
        toast.error("Date cannot be in the future");
        return;
      }
  
      if(!categoryId){
        toast.error("Please select a category");
        return;
      }
  
      try{
        const response = await axiosConfig.post(apiEndpoints.ADD_EXPENSE, {
          name,
          amount: Number(amount),
          date,
          icon,
          categoryId
        })
  
        if(response.status === 201){
          setOpenAddExpenseModal(false);
          toast.success("Expense added successfully");
          fetchExpenseDetails();
          fetchExpenseCategories();
        }
      }catch(error){
        console.log("Error adding expense", error);
        toast.error(error.response?.data?.message || "Failed to add expense");
      }
    }
  
  
    // delete expense details
    const deleteExpense = async (id) => {
      
      try{
        const response = await axiosConfig.delete(apiEndpoints.DELETE_EXPENSE(id));
        setOpenDeleteAlert({show:false, data:null});
        toast.success("Expense deleted successfully");
        fetchExpenseDetails();
      }catch(error){
        console.log("Error deleting expense", error);
        toast.error(error.response?.data?.message || "Failed to delete expense");
      }
    }
  
  
    const handleDownloadExpenseDetails = async () => {
      
      try{
        const response = await axiosConfig.get(apiEndpoints.EXPENSE_EXCEL_DOWNLOAD, {responseType:"blob"});
        let filename = "expense_details.xlsx"
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", filename);
        document.body.appendChild(link);
        link.click();
        link.parentNode.removeChild(link);
        window.URL.revokeObjectURL(url);
        toast.success("Download expense details successfully");
      }catch(error){
        console.error("Error downloading the expense details", error);
        toast.error(error.response?.data?.message ||  "Failed to download expense");
      }
    }
  
    const handleEmailExpenseDetails = async () => {
      
      try{
        const response = await axiosConfig.get(apiEndpoints.EMAIL_EXPENSE);
        if(response.status === 200){
          toast.success("Expense details emailed successfully");
        }
      }catch(error){
        console.error("Error emailing expense details: ", error);
        toast.error(error.response?.data?.message || "Failed to email expense");
      }
    }
  
  
    useEffect(()=> {
      fetchExpenseDetails();
      fetchExpenseCategories();
    }, []);
  
  
  return (
    <Dashboard activeMenu="Expenses">
      <div className="my-5 mx-auto">
        <div className="grid grid-cols-1 gap-6">
          <div>
            {/* overview for expense with line chart */}
            
            <ExpenseOverview transactions={expenseData} onAddExpense={()=> setOpenAddExpenseModal(true)}/>
          </div>

          <ExpenseList
          transactions={expenseData} onDelete={(id)=>setOpenDeleteAlert({show: true, data: id})}
          onDownload={handleDownloadExpenseDetails}
          onEmail={handleEmailExpenseDetails}
          />


          <Modal
          isOpen={openAddExpenseModal}
          onClose={()=> setOpenAddExpenseModal(false)}
          title="Add Expense"
          > 
           <AddExpenseForm
           onAddExpense={(expense)=> handleAddExpense(expense)}
           categories={categories}
           />
          </Modal>

          {/* Delete expense modal */}
          <Modal
          isOpen={openDeleteAlert.show}
          onClose={() => setOpenDeleteAlert({show: false, data: null})}
          title="Delete Expense"
          >
            <DeleteAlert
            content="Are you sure you want to delete this expense?"
            onDelete={()=> deleteExpense(openDeleteAlert.data)}
            />
          </Modal>
        </div>
      </div>
    </Dashboard>
  )
}

export default Expense