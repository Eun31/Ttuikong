<template>
    <div v-if="show" class="modal-overlay" @click="handleCancel">
        <div class="confirm-modal" @click.stop>
            <div class="modal-header">
                <h3>{{ title }}</h3>
            </div>

            <div class="modal-content">
                <p>{{ message }}</p>
                <p v-if="warning" class="warning-text">{{ warning }}</p>
            </div>

            <div class="modal-actions">
                <button @click="handleCancel" class="cancel-btn">
                    {{ cancelText }}
                </button>
                <button @click="handleConfirm" class="confirm-btn" :class="{ danger: isDanger }" :disabled="loading">
                    <span v-if="loading" class="spinning"></span>
                    <span v-else>{{ confirmText }}</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';

// Props 정의
const props = defineProps({
    show: {
        type: Boolean,
        default: false
    },
    title: {
        type: String,
        default: '확인'
    },
    message: {
        type: String,
        required: true
    },
    warning: {
        type: String,
        default: ''
    },
    confirmText: {
        type: String,
        default: '확인'
    },
    cancelText: {
        type: String,
        default: '취소'
    },
    isDanger: {
        type: Boolean,
        default: false
    },
    loading: {
        type: Boolean,
        default: false
    }
});

// Emit 정의
const emit = defineEmits(['confirm', 'cancel']);

// 메서드
function handleConfirm() {
    if (!props.loading) {
        emit('confirm');
    }
}

function handleCancel() {
    if (!props.loading) {
        emit('cancel');
    }
}
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    padding: 20px;
    animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

.confirm-modal {
    background: white;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
    max-width: 400px;
    width: 90%;
    overflow: hidden;
    animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.modal-header {
    padding: 20px 20px 16px;
    border-bottom: 1px solid #e5e5e5;
    background: linear-gradient(135deg, rgba(255, 87, 34, 0.05) 0%, transparent 100%);
}

.modal-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #333;
}

.modal-content {
    padding: 20px;
}

.modal-content p {
    margin: 0;
    font-size: 15px;
    line-height: 1.5;
    color: #555;
}

.warning-text {
    font-size: 13px;
    color: #ef4444;
    margin-top: 12px;
    font-weight: 500;
}

.modal-actions {
    display: flex;
    gap: 12px;
    padding: 16px 20px 20px;
    border-top: 1px solid #e5e5e5;
    background: #fafafa;
}

.cancel-btn,
.confirm-btn {
    flex: 1;
    padding: 12px 0;
    border: none;
    border-radius: 8px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.cancel-btn {
    background: #f5f5f5;
    color: #333;
    border: 1px solid #e0e0e0;
}

.cancel-btn:hover {
    background: #e5e5e5;
    border-color: #d0d0d0;
}

.confirm-btn {
    background: #FF7E47;
    color: white;
}

.confirm-btn:hover:not(:disabled) {
    background: #e85d2a;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 126, 71, 0.3);
}

.confirm-btn.danger {
    background: #ef4444;
}

.confirm-btn.danger:hover:not(:disabled) {
    background: #dc2626;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.confirm-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

.spinning {
    animation: spin 1s linear infinite;
    display: inline-block;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

/* 모바일 반응형 */
@media (max-width: 480px) {
    .modal-overlay {
        padding: 16px;
    }

    .confirm-modal {
        border-radius: 12px;
    }

    .modal-header {
        padding: 16px 16px 12px;
    }

    .modal-header h3 {
        font-size: 16px;
    }

    .modal-content {
        padding: 16px;
    }

    .modal-content p {
        font-size: 14px;
    }

    .modal-actions {
        padding: 12px 16px 16px;
        gap: 10px;
    }

    .cancel-btn,
    .confirm-btn {
        padding: 10px 0;
        font-size: 13px;
    }
}
</style>