import React, { useEffect, useState } from 'react'
import EmojiPickerPopup from './EmojiPickerPopup';
import Input from './Input';
import { LoaderCircle } from 'lucide-react';

const AddExpenseForm = ({onAddExpense, categories}) => {

    const [expense, setExpense] = useState({
            name: "",
            amount: "",
            date: "",
            icon: "",
            categoryId: ""
        })
    
        const [loading, setLoading] = useState(false);
    
        const categoryOptions = categories.map(category => ({
            value: category.id,
            label: category.name
        }));
    
        const handleChange = (key, value) => {
            setExpense({...expense, [key]: value});
        }
    
        const handleAddExpense = async () => {
            setLoading(true);
            try {
                // Format date from yyyy-MM-dd to dd-MM-yyyy
                const formattedExpense = {
                    ...expense,
                    date: expense.date ? expense.date.split('-').reverse().join('-') : ''
                };
                await onAddExpense(formattedExpense);
            } finally {
                setLoading(false);
            }
        }
    
        useEffect(()=>{
            if(categories.length > 0 && !expense.categoryId){
                setExpense((prev) => ({...prev, categoryId: categories[0].id}))
            }
        }, [categories, expense.categoryId]);

  return (
    <div>
        <EmojiPickerPopup
        icon={expense.icon}
        onSelect={(selectedIcon)=>handleChange("icon", selectedIcon)}
        />

        <Input
        value={expense.name}
        onChange={({target}) => handleChange("name", target.value)}
        label="Expense Name"
        placeholder="e.g Transportation, Dates, Gas"
        type="text"
        />

        <Input
        label="Category"
        value={expense.categoryId}
        onChange={({target}) => handleChange("categoryId", target.value)}
        isSelect={true}
        options={categoryOptions}
        />

        <Input
        value={expense.amount}
        onChange={({target}) => handleChange("amount", target.value)}
        label="Amount"
        placeholder="e.g 5000"
        type="number"
        />

        <Input
        value={expense.date}
        onChange={({target}) => handleChange("date", target.value)}
        label="Date"
        placeholder=""
        type="date"
        />

        <div className="flex justify-end mt-6">
            <button
            onClick={handleAddExpense}
            disabled={loading}
            className='add-btn add-btn-fill'>
                {loading ? (
                    <>
                    <LoaderCircle className="w-4 h-4 animate-spin"/>
                    Adding...
                    </>
                ): (
                    <>
                    Add Expense
                    </>
                )}
            </button>
        </div>
    </div>
  )
}

export default AddExpenseForm