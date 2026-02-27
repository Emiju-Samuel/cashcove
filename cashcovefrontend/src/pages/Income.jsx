import React, { useEffect, useState } from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'
import axiosConfig from '../util/axiosConfig';
import { apiEndpoints } from '../util/apiEndpoints';
import IncomeList from '../Components/IncomeList';
import Modal from '../Components/Modal';
import { Plus } from 'lucide-react';
import axios from 'axios';
import AddIncomeForm from '../Components/AddIncomeForm';
import { toast } from 'react-toastify';
import DeleteAlert from '../Components/DeleteAlert';
import IncomeOverview from '../Components/IncomeOverview';

const Income = () => {
  UseUser();

  const [incomeData, setIncomeData] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);

  const [openAddIncomeModal, setOpenAddIncomeModal] = useState(false);
  const [openDeleteAlert, setOpenDeleteAlert] = useState({
    show: false,
    data: null,
  })

  // Fetch income details from the API
  const fetchIncomeDetails = async () => {
    if(loading) return;
    setLoading(true);

    try{
    const response = await axiosConfig.get(apiEndpoints.GET_ALL_INCOMES);
    if(response.status === 200){
      setIncomeData(response.data);
    }
  }catch(error){
    console.error("Failed to fetch income details:", error);
    toast.error(error.response?.data?.message || "failed to fetch income details");
  }finally{
    setLoading(false);
  }
  }

  // fetch categories for income
  const fetchIncomeCategories = async () => {
    try{
      const response = await axiosConfig.get(apiEndpoints.CATEGORY_BY_TYPE("income"));
      if(response.status === 200){
        console.log("Income categories", response.data)
        setCategories(response.data);
      }
    }catch(error){
      console.log("Failed to fetch income categories: ", error);
      toast.error(error.data?.message || "Failed to fetch income categories");
    }
  }


  // function for adding new income
  const handleAddIncome = async (income) => {
    const {name, amount, date, icon, categoryId} = income;

    // input validation
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

    // API call for adding new income
    try{
      const response = await axiosConfig.post(apiEndpoints.ADD_INCOME, {
        name,
        amount: Number(amount),
        date,
        icon,
        categoryId
      })

      if(response.status === 201){
        setOpenAddIncomeModal(false);
        toast.success("Income added successfully");
        fetchIncomeDetails();
        fetchIncomeCategories();
      }
    }catch(error){
      console.log("Error adding income", error);
      toast.error(error.response?.data?.message || "Failed to add income");
    }
  }


  // delete income details
  const deleteIncome = async (id) => {
    
    try{
      const response = await axiosConfig.delete(apiEndpoints.DELETE_INCOME(id));
      setOpenDeleteAlert({show:false, data:null});
      toast.success("Income deleted successfully");
      fetchIncomeDetails();
    }catch(error){
      console.log("Error deleting income", error);
      toast.error(error.response?.data?.message || "Failed to delete income");
    }
  }


  const handleDownloadIncomeDetails = async () => {
    
    try{
      const response = await axiosConfig.get(apiEndpoints.INCOME_EXCEL_DOWNLOAD, {responseType:"blob"});
      let filename = "income_details.xlsx"
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success("Download income details successfully");
    }catch(error){
      console.error("Error downloading the income details", error);
      toast.error(error.response?.data?.message ||  "Failed to download income");
    }
  }

  const handleEmailIncomeDetails = async () => {
    
    try{
      const response = await axiosConfig.get(apiEndpoints.EMAIL_INCOME);
      if(response.status === 200){
        toast.success("Income details emailed successfully");
      }
    }catch(error){
      console.error("Error emailing income details: ", error);
      toast.error(error.response?.data?.message || "Failed to email income");
    }
  }


  useEffect(()=> {
    fetchIncomeDetails();
    fetchIncomeCategories();
  }, []);



  return (
    <Dashboard activeMenu="Income">
      <div className="my-5 mx-auto">
        <div className="grid grid-cols-1 gap-6">
          <div>
            {/* overview for income with line chart */}
            
            <IncomeOverview transactions={incomeData} onAddIncome={()=> setOpenAddIncomeModal(true)}/>
          </div>

          <IncomeList
          transactions={incomeData} onDelete={(id)=>setOpenDeleteAlert({show: true, data: id})}
          onDownload={handleDownloadIncomeDetails}
          onEmail={handleEmailIncomeDetails}
          />


          <Modal
          isOpen={openAddIncomeModal}
          onClose={()=> setOpenAddIncomeModal(false)}
          title="Add Income"
          > 
           <AddIncomeForm
           onAddIncome={(income)=> handleAddIncome(income)}
           categories={categories}
           />
          </Modal>
          

          {/* Delete income modal */}
          <Modal
          isOpen={openDeleteAlert.show}
          onClose={() => setOpenDeleteAlert({show: false, data: null})}
          title="Delete Income"
          >
            <DeleteAlert
            content="Are you sure you want to delete this income?"
            onDelete={()=> deleteIncome(openDeleteAlert.data)}
            />
          </Modal>
        </div>
      </div>
    </Dashboard>
  )
}

export default Income