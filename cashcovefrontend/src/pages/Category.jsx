import React, { useEffect, useState } from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'
import { Hand, Plus } from 'lucide-react';
import CategoryList from '../Components/CategoryList';
import axiosConfig from '../util/axiosConfig';
import { apiEndpoints } from '../util/apiEndpoints';
import Modal from '../Components/Modal';
import AddCategoryForm from '../Components/AddCategoryForm';
import { toast } from 'react-toastify';

const Category = () => {

  const [loading, setIsLoading] = useState(false);
  const [categoryData, setCategoryData] = useState([]);
  const [openAddCategoryModal, setOpenAddCategoryModal] = useState(false);
  const [openEditCategoryModal, setOpenEditCategoryModal] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);

  UseUser();

  const fetchCategoryDetails = async () => {
    if(loading) return;
    setIsLoading(true);

    try{
      const response = await axiosConfig.get(apiEndpoints.GET_ALL_CATEGORIES);
    if(response.status === 200){
      console.log('categories', response.data);
      setCategoryData(response.data);
    }
    }catch(error){
      console.error("Something went wrong. Please try again", error);
      toast.error(error.message);
    }finally{
      setIsLoading(false);
    }
  }

  useEffect(() => {
    fetchCategoryDetails();
  }, []);

  const handleAddCategory = async (category) => {
    const {name, type, icon} = category;
    if(!name.trim()){
      toast.error("Category name is required");
      return;
    }

    // check if the category already exists
    const isDuplicate = categoryData.some((category) => {
      return category.name.toLowerCase() === name.trim().toLowerCase();
    })

    if(isDuplicate){
      toast.error("Category name already exists");
      return;
    }

    try{
      const response = await axiosConfig.post(apiEndpoints.ADD_CATEGORY, {name, type, icon});
      if(response.status === 201){
        toast.success("Category added successfully");
        setOpenAddCategoryModal(false);
        fetchCategoryDetails();
      }
    }catch(error){
      console.error("Error adding category:", error);
      toast.error(error.response?.data?.message || "Failed to add category")
    }
  }


  const handleEditcategory = (categoryToEdit) => {
    setSelectedCategory(categoryToEdit);
    setOpenEditCategoryModal(true);
  }

  const handleUpdateCategory = async (updatedCategory) => {
    const {id, name, type, icon} = updatedCategory;
    if(!name.trim()){
      toast.error("Category Name is required");
      return;
    }

    if(!id){
      toast.error("Category ID is missing");
      return;
    }

    try{
      const response = await axiosConfig.put(apiEndpoints.UPDATE_CATEGORY(id), {name, type, icon});
      setOpenAddCategoryModal(false);
      setSelectedCategory(null);
      toast.success("Category updated successfully");
      fetchCategoryDetails();
      setOpenEditCategoryModal(false);
    }catch(error){
      console.error("Error updating category: ", error.response?.data?.message || error.message);
      toast.error(error.response?.data?.message || "Failed to update category");
    }
  }

  return (
    <Dashboard activeMenu="Category">
      <div className="my-5 mx-auto">
        {/* add button to add category */}

        <div className="flex justify-between items-center mb-5 bg-white rounded-xl p-5 shadow">
          <h2 className="text-2xl font-semibold">All Categories</h2>
          <button
          onClick={()=> setOpenAddCategoryModal(true)}
          className="rounded p-2 add-btn flex items-center gap-1">
            <Plus className="text-green-500" size={15}/>
            New Category
          </button>
        </div>
        
        {/* catgeory list */}
        <CategoryList categories={categoryData} onEditCategory={handleEditcategory}/>


        {/* adding category modal */}

        <Modal
        isOpen={openAddCategoryModal}
        onClose={()=>setOpenAddCategoryModal(false)}
        title="Add Category"
        >
          <AddCategoryForm onAddCategory={handleAddCategory}/>
        </Modal>
        {/* updating category modal */}
        <Modal
        onClose={() => {
          setOpenEditCategoryModal(false);
          setSelectedCategory(null);
        }}
        isOpen={openEditCategoryModal}
        title="Update Category"
        >
          <AddCategoryForm
          initialCategoryData={selectedCategory}
            onAddCategory={handleUpdateCategory}
            isEditing={true}
          />
        </Modal>
      </div>
    </Dashboard>
  )
}

export default Category