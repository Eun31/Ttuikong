<template>
  <div class="run-container">
    <RunTimer v-if="currentView === 'RunTimer'" @navigate="navigateTo" />
    <RunWithCrew v-else-if="currentView === 'RunWithCrew'" @navigate="navigateTo" />
    <RunWithRank v-else-if="currentView === 'RunWithRank'" @navigate="navigateTo" />
  </div>
</template>

<script setup>
import { ref, watchEffect } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import RunTimer from './RunTimer.vue';
import RunWithCrew from './RunWithCrew.vue';
import RunWithRank from './RunWithRank.vue';

const props = defineProps({
  view: {
    type: String,
    default: null
  }
});

const router = useRouter();
const currentView = ref('RunTimer');
const viewHistory = ref(['RunTimer']);

watchEffect(() => {
  if (props.view) {
    switch (props.view) {
      case 'timer':
        currentView.value = 'RunTimer';
        break;
      case 'crew':
        currentView.value = 'RunWithCrew';
        break;
      case 'rank':
        currentView.value = 'RunWithRank';
        break;
      default:
        currentView.value = 'RunTimer';
    }
    viewHistory.value = [currentView.value];
  }
});

const navigateTo = (view) => {
  viewHistory.value.push(currentView.value);
  currentView.value = view;
};

const goBack = () => {
  if (viewHistory.value.length > 1) {
    currentView.value = viewHistory.value.pop();
  } else {
    router.go(-1);
  }
};
</script>


<style scoped>
.run-container {
  min-height: 100vh;
  background-color: white;
}

/* 애니메이션 효과 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-leave-to,
.fade-enter-from {
  opacity: 0;
}
</style>