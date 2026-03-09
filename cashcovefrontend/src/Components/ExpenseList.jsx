import { Download, LoaderCircle, Mail } from 'lucide-react';
import React, { useState } from 'react'
import TransactionInfoCard from './TransactionInfoCard';
import moment from 'moment';

const ExpenseList = ({transactions, onDelete, onDownload, onEmail}) => {

    const [isDownloading, setIsDownloading] = useState(false);
    const [isEmailing, setIsEmailing] = useState(false);
    
        const handleEmail = async () => {
            setIsEmailing(true);
            try{
                await onEmail();
            }finally{
                setIsEmailing(false);
            }
        }
    
        const handleDownload = async () => {
            setIsDownloading(true);
            try{
                await onDownload();
            }finally{
                setIsDownloading(false);
            }
        }

  return (
    <div className="card mt-5 mb-8 bg-white py-5 px-4 sm:px-8 rounded-xl shadow-md shadow-gray-50 border border-gray-200/50 gap-3 sm:gap-6 flex flex-col">
        <div className="flex items-center justify-between">
            <h5 className="text-lg">My Expenses</h5>
            <div className="flex items-center justify-end gap-2.5 sm:gap-6">
                <button disabled={isEmailing} className="card-btn flex items-center justify-end gap-2" onClick={handleEmail}>
                    {isEmailing ? (
                        <>
                        <LoaderCircle className='w-4 h-4 animate-spin'/>
                        Emailing....
                        </>
                    ):(
                       <>
                       <Mail size={15} className='text-base invisible sm:visible'/> Email
                       </> 
                    )}
                </button>

                <button disabled={isDownloading} className="card-btn flex items-center justify-end gap-2" onClick={handleDownload}>
                    {isDownloading ? (
                        <>
                        <LoaderCircle className='w-4 h-4 animate-spin'/>
                        Downloading...
                        </>
                    ):(
                        <>
                        <Download size={15} className='text-base invisible sm:visible'/> Download
                        </>
                    )}
                </button>
            </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2">
            {/* display the expenses */}
            {transactions?.map((expense)=> (
                <TransactionInfoCard
                key={expense.id}
                title={expense.name}
                icon={expense.icon}
                date={moment(expense.date).format("Do MMM YYYY")}
                amount={expense.amount}
                type="Expense"
                onDelete={()=>onDelete(expense.id)}
                />
            ))}
        </div>
    </div>
  )
}

export default ExpenseList