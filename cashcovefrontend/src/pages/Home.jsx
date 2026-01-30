import React, { useEffect, useState } from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'
import InfoCard from '../Components/InfoCard';
import { addThousandsSeperator } from '../Components/util';
import { Coins, Wallet, WalletCards } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axiosConfig from '../util/axiosConfig';
import { apiEndpoints } from '../util/apiEndpoints';
import { toast } from 'react-toastify';
import RecentTransactions from '../Components/RecentTransactions';
import FinanceOverview from '../Components/FinanceOverview';
import Transactions from '../Components/Transactions';

const Home = () => {


  UseUser();

  const navigate = useNavigate();

  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchDashboardData = async () => {
    if(loading) return;

    setLoading(true);

    try{
      const response = await axiosConfig.get(apiEndpoints.DASHBOARD_DATA);
      if(response.status === 200){
        setDashboardData(response.data);
      }
    }catch(error){
      console.error("Something went wrong while fetching daahboard data: ", error);
      toast.error("Something went wrong");
    }finally{
      setLoading(false);
    }
  }

  useEffect(()=> {
    fetchDashboardData();
    return () => {};
  }, []);

  return (
    <div>
      <Dashboard activeMenu="Dashboard">
        <div className="my-5 mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

            <InfoCard
            icon={<WalletCards/>}
            label="Total Balance"
            value={addThousandsSeperator(dashboardData?.totalBalance || "")}
            color="bg-purple-800"
            />

            <InfoCard
            icon={<Wallet/>}
            label="Total Income"
            value={addThousandsSeperator(dashboardData?.totalIncome || "")}
            color="bg-green-800"
            />

            <InfoCard
            icon={<Coins/>}
            label="Total Expense"
            value={addThousandsSeperator(dashboardData?.totalExpense || "")}
            color="bg-red-800"
            />


          </div>
          <div className='grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-2 mt-6'>
            {/* Recent transactions */}
            <RecentTransactions
              transactions={dashboardData?.recentTransactions}
              onMore={()=> navigate("/expense")}
            />
            {/* Finance overview chart */}
            <FinanceOverview
            totalBalance={dashboardData?.totalBalance || 0}
            totalIncome={dashboardData?.totalIncome || 0}
            totalExpense={dashboardData?.totalExpense || 0}
            />
            {/* Expense transactions */}
            <Transactions
            transactions={dashboardData?.recent5Expenses || []}
            onMore={()=>navigate("/expense")}
            type="expense"
            title="Recent Expenses"
            />
            {/* Income trnsactions */}
            <Transactions
            transactions={dashboardData?.recent5Incomes || []}
            onMore={()=>navigate("/income")}
            type="income"
            title="Recent Incomes"
            />
          </div>
        </div>
      </Dashboard>
    </div>
  )
}

export default Home