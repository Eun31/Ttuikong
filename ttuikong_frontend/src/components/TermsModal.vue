<template>
    <div v-if="visible" class="modal-overlay" @click.self="close">
        <div class="modal-content">
            <button class="close-btn" @click="close">×</button>
            <h2 class="modal-title">{{ title }}</h2>
            <pre class="modal-text">{{ content }}</pre>
        </div>
    </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
    visible: Boolean,
    title: String,
    content: String
});

const emit = defineEmits(['close']);

const close = () => {
    emit('close');
};
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 80%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 999;
    padding: 1rem;
    box-sizing: border-box;
}

.modal-content {
    background: #fff;
    padding: 1.5rem;
    width: 100%;
    max-width: 360px;
    border-radius: 12px;
    position: relative;
    max-height: 80vh;
    overflow-y: auto;
    word-break: break-word;
    white-space: pre-wrap;
    font-size: 14px;
    line-height: 1.5;
    box-sizing: border-box;
}

.modal-title {
    font-size: 1.1rem;
    margin-bottom: 1rem;
    font-weight: bold;
    text-align: center;
}

.modal-text {
    font-size: 0.95rem;
    white-space: pre-wrap;
    line-height: 1.6;
}

.close-btn {
    position: absolute;
    top: 0.8rem;
    right: 0.8rem;
    font-size: 1.5rem;
    background: none;
    border: none;
    cursor: pointer;
    color: #666;
}
</style
