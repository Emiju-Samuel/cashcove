import { Edit2, Trash2 } from 'lucide-react';
import StatusChip from './StatusChip';
import FrequencyBadge from './FrequencyBadge';
import RenewalCountdown from './RenewalCountdown';

const SubscriptionCard = ({ subscription, onEdit, onDelete }) => {
  const { icon, subscriptionName, amount, frequency, nextRenewalDate, subscriptionStatus } = subscription;

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  return (
    <div className="bg-white rounded-lg shadow p-4 hover:shadow-lg transition-shadow">
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-3">
          <div className="text-3xl">{icon || '📦'}</div>
          <div>
            <h3 className="font-semibold text-gray-900">{subscriptionName}</h3>
            <FrequencyBadge frequency={frequency} />
          </div>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => onEdit(subscription)}
            className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded transition"
            title="Edit subscription"
          >
            <Edit2 size={18} />
          </button>
          <button
            onClick={() => {
              if (window.confirm('Are you sure you want to delete this subscription?')) {
                onDelete(subscription.id);
              }
            }}
            className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded transition"
            title="Delete subscription"
          >
            <Trash2 size={18} />
          </button>
        </div>
      </div>

      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-2xl font-bold text-gray-900">
            {formatCurrency(amount)}
          </span>
          <StatusChip status={subscriptionStatus} />
        </div>

        <div className="pt-2 border-t border-gray-200">
          <p className="text-xs text-gray-600 mb-2">Next Renewal</p>
          <RenewalCountdown nextRenewalDate={nextRenewalDate} />
        </div>
      </div>
    </div>
  );
};

export default SubscriptionCard;
