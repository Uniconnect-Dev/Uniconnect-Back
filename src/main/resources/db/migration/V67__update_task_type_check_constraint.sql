ALTER TABLE collaboration_tasks
DROP CONSTRAINT IF EXISTS collaboration_tasks_type_check;

ALTER TABLE collaboration_tasks
ADD CONSTRAINT collaboration_tasks_type_check
CHECK (
    type IN (
        'ProductInfo',
        'ContentShare',
        'ShippingInfo',
        'ReceiveInfo',
        'Receipt',
        'EventDateFix'
    )
);
