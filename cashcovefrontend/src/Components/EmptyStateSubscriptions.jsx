import { Plus } from 'lucide-react';

const EmptyStateSubscriptions = ({ onAddClick }) => {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <div className="text-6xl mb-4">📭</div>
      <h3 className="text-2xl font-bold text-gray-900 mb-2">No Subscriptions Yet</h3>
      <p className="text-gray-600 mb-6 max-w-sm">
        Start tracking your recurring payments and get reminders before they renew.
      </p>
      <button
        onClick={onAddClick}
        className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition shadow-md hover:shadow-lg"
      >
        <Plus size={20} /> Add Your First Subscription
      </button>
    </div>
  );
};

export default EmptyStateSubscriptions;
