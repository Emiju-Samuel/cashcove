import React, { useEffect, useState } from 'react'
import Input from './Input'
import EmojiPickerPopup from './EmojiPickerPopup'
import { LoaderCircle } from 'lucide-react'

const AddCategoryForm = ({onAddCategory, initialCategoryData, isEditing}) => {

    const [category, setCategory] = useState({
        name:"",
        type: "",
        icon: ""
    })

    const [loading, setLoading] = useState(false);

    useEffect(()=> {
        if(isEditing && initialCategoryData){
            setCategory(initialCategoryData);
        }else{
            setCategory({name: "", type: "", icon: ""})
        }
    }, [isEditing, initialCategoryData])

    const categoryTypeOptions = [
        {value: "Income", label: "Income"},
        {value: "Expense", label: "Expense"},
    ]

    const handleChange = (key, value) => {
        setCategory({...category, [key]: value})
    }

    const handleSubmit = async () => {
        setLoading(true);
        try{
            await onAddCategory(category);
        }finally{
            setLoading(false);
        }
    }

  return (
    <div className='p-4'>

        <EmojiPickerPopup
        icon={category.icon}
        onSelect={(selectedIcon)=>handleChange("icon", selectedIcon)}
        />
        <Input
        value={category.name}
        onChange={({target}) => handleChange("name", target.value)}
        label="Category Name"
        placeholder="e.g., Salary, Transportation, Groceries"
        type="text"
        />

        <Input
        label="Category Type"
        value={category.type}
        onChange={({target}) => handleChange("type", target.value)}
        isSelect={true}
        options={categoryTypeOptions}
        />

        <div className="flex justify-end mt-6">
            <button
            type='button'
            onClick={handleSubmit}
            disabled={loading}
            className='add-btn add-btn-fill'
            >
                {loading ? (
                    <>
                    <LoaderCircle className='w-4 h-4 animate-spin'/>
                    {isEditing ? "Updating" : "Adding..."}
                    </>
                ):(
                    <>
                    {isEditing ? "Update category" : "Add Category"}
                    </>
                )}
            </button>
        </div>
    </div>
  )
}

export default AddCategoryForm