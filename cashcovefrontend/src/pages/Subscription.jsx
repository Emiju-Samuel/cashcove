import React, { useState, useEffect } from 'react';
import { UseUser } from '../hooks/UseUser';
import Dashboard from '../Components/Dashboard';
import { baseUrl, apiEndpoints } from '../util/apiEndpoints';
import axiosConfig from '../util/axiosConfig';
import Modal from '../Components/Modal';
import DeleteAlert from '../Components/DeleteAlert';
import SubscriptionForm from '../Components/SubscriptionForm';
import SubscriptionCard from '../Components/SubscriptionCard';
import SubscriptionTable from '../Components/SubscriptionTable';
import SubscriptionStatsCards from '../Components/SubscriptionStatsCards';
import UpcomingRenewalCard from '../Components/UpcomingRenewalCard';
import EmptyStateSubscriptions from '../Components/EmptyStateSubscriptions';
import { Plus, LayoutGrid, List } from 'lucide-react';

const Subscription = () => {
  UseUser();

  // State Management
  const [subscriptions, setSubscriptions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [viewMode, setViewMode] = useState('grid'); // 'grid' or 'table'
  const [error, setError] = useState(null);

  // Modal States
  const [openAddModal, setOpenAddModal] = useState(false);
  const [openEditModal, setOpenEditModal] = useState(false);
  const [openDeleteAlert, setOpenDeleteAlert] = useState({ show: false, data: null });
  const [openStatusModal, setOpenStatusModal] = useState({ show: false, subscription: null });

  // Form Data
  const [selectedSubscription, setSelectedSubscription] = useState(null);
  const [newStatus, setNewStatus] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Fetch Subscriptions
  const fetchSubscriptions = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axiosConfig.get(apiEndpoints.GET_ALL_SUBSCRIPTIONS);
      setSubscriptions(response.data || []);
    } catch (err) {
      setError('Failed to fetch subscriptions. Please try again.');
      console.error('Error fetching subscriptions:', err);
    } finally {
      setLoading(false);
    }
  };

  // Fetch Categories
  const fetchCategories = async () => {
    try {
      const response = await axiosConfig.get(apiEndpoints.GET_ALL_CATEGORIES);
      setCategories(response.data || []);
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  useEffect(() => {
    fetchSubscriptions();
    fetchCategories();
  }, []);

  // Add Subscription
  const handleAddSubscription = async (formData) => {
    setIsSubmitting(true);
    try {
      const payload = {
        icon: formData.icon,
        subscriptionName: formData.subscriptionName,
        amount: formData.amount,
        frequency: formData.frequency,
        startDate: formData.startDate,
        nextRenewalDate: formData.nextRenewalDate,
        reminderDaysBefore: formData.reminderDaysBefore,
        categoryId: formData.categoryId,
      };

      await axiosConfig.post(apiEndpoints.ADD_SUBSCRIPTION, payload);
      setOpenAddModal(false);
      await fetchSubscriptions();
    } catch (err) {
      setError('Failed to add subscription. Please try again.');
      console.error('Error adding subscription:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Edit Subscription
  const handleEditSubscription = async (formData) => {
    setIsSubmitting(true);
    try {
      const payload = {
        icon: formData.icon,
        subscriptionName: formData.subscriptionName,
        amount: formData.amount,
        frequency: formData.frequency,
        startDate: formData.startDate,
        nextRenewalDate: formData.nextRenewalDate,
        reminderDaysBefore: formData.reminderDaysBefore,
        categoryId: formData.categoryId,
      };

      await axiosConfig.put(
       apiEndpoints.UPDATE_SUBSCRIPTION(selectedSubscription.id),
        payload
      );
      setOpenEditModal(false);
      setSelectedSubscription(null);
      await fetchSubscriptions();
    } catch (err) {
      setError('Failed to update subscription. Please try again.');
      console.error('Error updating subscription:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Delete Subscription
  const deleteSubscription = async (id) => {
    setIsSubmitting(true);
    try {
      await axiosConfig.delete(apiEndpoints.DELETE_SUBSCRIPTION(id));
      setOpenDeleteAlert({ show: false, data: null });
      await fetchSubscriptions();
    } catch (err) {
      setError('Failed to delete subscription. Please try again.');
      console.error('Error deleting subscription:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Change Status
  const handleChangeStatus = async () => {
    setIsSubmitting(true);
    try {
      await axiosConfig.post(
        apiEndpoints.CHANGE_SUBSCRIPTION_STATUS(selectedSubscription.id),
        null,
        { params: { status: newStatus } }
      );
      setOpenStatusModal({ show: false, subscription: null });
      setNewStatus('');
      await fetchSubscriptions();
    } catch (err) {
      setError('Failed to update subscription status. Please try again.');
      console.error('Error changing status:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Get upcoming renewals (within 7 days)
  const upcomingRenewals = subscriptions
    .filter((sub) => {
      const renewal = new Date(sub.nextRenewalDate);
      const today = new Date();
      const daysLeft = Math.ceil((renewal - today) / (1000 * 60 * 60 * 24));
      return daysLeft <= 7 && daysLeft > 0;
    })
    .sort((a, b) => new Date(a.nextRenewalDate) - new Date(b.nextRenewalDate));

  return (
    <Dashboard activeMenu="Subscriptions">
      <div className="my-6 mx-auto max-w-7xl px-4">
        {/* Header */}
        <div className="flex flex-col md:flex-row items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Subscriptions</h1>
            <p className="text-gray-600 mt-1">Manage and track all your recurring payments</p>
          </div>
          <button
            onClick={() => {
              setOpenAddModal(true);
              setSelectedSubscription(null);
            }}
            className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition shadow-md hover:shadow-lg"
          >
            <Plus size={20} /> Add Subscription
          </button>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 flex items-center justify-between">
            <span>{error}</span>
            <button onClick={() => setError(null)} className="text-red-500 hover:text-red-700">
              ✕
            </button>
          </div>
        )}

        {/* Stats */}
        {subscriptions.length > 0 && <SubscriptionStatsCards subscriptions={subscriptions} />}

        {/* Upcoming Renewals */}
        {upcomingRenewals.length > 0 && (
          <div className="mt-8">
            <h2 className="text-xl font-bold text-gray-900 mb-4">Upcoming Renewals</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {upcomingRenewals.slice(0, 3).map((sub) => (
                <UpcomingRenewalCard
                  key={sub.id}
                  subscription={sub}
                  onHandle={(subscription) => {
                    setSelectedSubscription(subscription);
                    setOpenEditModal(true);
                  }}
                />
              ))}
            </div>
          </div>
        )}

        {/* Main Content */}
        <div className="mt-8">
          {subscriptions.length === 0 ? (
            <div className="bg-white rounded-lg shadow">
              <EmptyStateSubscriptions onAddClick={() => setOpenAddModal(true)} />
            </div>
          ) : (
            <>
              {/* View Mode Toggle */}
              <div className="flex items-center gap-2 mb-4">
                <button
                  onClick={() => setViewMode('grid')}
                  className={`p-2 rounded-lg transition ${
                    viewMode === 'grid'
                      ? 'bg-blue-100 text-blue-600'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                  title="Grid view"
                >
                  <LayoutGrid size={20} />
                </button>

                <button
                  onClick={() => setViewMode('table')}
                  className={`p-2 rounded-lg sm:hidden transition ${
                    viewMode === 'table'
                      ? 'bg-blue-100 text-blue-600'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                  title="Table view"
                >
                  <List size={20} />
                </button>
              </div>

              {/* Grid View */}
              {viewMode === 'grid' && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {subscriptions.map((subscription) => (
                    <SubscriptionCard
                      key={subscription.id}
                      subscription={subscription}
                      onEdit={(sub) => {
                        setSelectedSubscription(sub);
                        setOpenEditModal(true);
                      }}
                      onDelete={(id) => setOpenDeleteAlert({ show: true, data: id })}
                    />
                  ))}
                </div>
              )}

              {/* Table View */}
              {viewMode === 'table' && (
                <div className="-mx-4 overflow-x-auto">
                  <div className="px-4">
                    <SubscriptionTable
                      subscriptions={subscriptions}
                      isLoading={loading}
                      onEdit={(sub) => {
                        setSelectedSubscription(sub);
                        setOpenEditModal(true);
                      }}
                      onDelete={(id) => setOpenDeleteAlert({ show: true, data: id })}
                    />
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* Add Subscription Modal */}
        <Modal
          isOpen={openAddModal}
          onClose={() => {
            setOpenAddModal(false);
            setSelectedSubscription(null);
          }}
          title="Add New Subscription"
        >
          <SubscriptionForm
            initialData={null}
            categories={categories}
            onSubmit={handleAddSubscription}
            onCancel={() => {
              setOpenAddModal(false);
              setSelectedSubscription(null);
            }}
            isLoading={isSubmitting}
          />
        </Modal>

        {/* Edit Subscription Modal */}
        <Modal
          isOpen={openEditModal}
          onClose={() => {
            setOpenEditModal(false);
            setSelectedSubscription(null);
          }}
          title="Edit Subscription"
        >
          <SubscriptionForm
            initialData={selectedSubscription}
            categories={categories}
            onSubmit={handleEditSubscription}
            onCancel={() => {
              setOpenEditModal(false);
              setSelectedSubscription(null);
            }}
            isLoading={isSubmitting}
          />
        </Modal>

        {/* Delete Confirmation */}
        <Modal
          isOpen={openDeleteAlert.show}
          onClose={() => setOpenDeleteAlert({ show: false, data: null })}
          title="Delete Subscription"
        >
          <DeleteAlert
            content="Are you sure you want to delete this subscription? This action cannot be undone."
            onDelete={() => deleteSubscription(openDeleteAlert.data)}
          />
        </Modal>
      </div>
    </Dashboard>
  );
};

export default Subscription;