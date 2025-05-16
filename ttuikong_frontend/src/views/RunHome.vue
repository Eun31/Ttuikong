
<template>
  <div class="run-container">
    <RunTimer v-if="currentView === 'RunTimer'" @navigate="navigateTo" />
    <RunWithCrew v-else-if="currentView === 'RunWithCrew'" @navigate="navigateTo" />
    <RunWithRank v-else-if="currentView === 'RunWithRank'" @navigate="navigateTo" />
  </div>
</template>

<script>
import RunTimer from './RunTimer.vue';
import RunWithCrew from './RunWithCrew.vue';
import RunWithRank from './RunWithRank.vue';

export default {
  name: 'RunHome',
  components: {
    RunTimer,
    RunWithCrew,
    RunWithRank
  },
  props: {
    view: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      currentView: 'RunTimer',
      viewHistory: ['RunTimer']
    }
  },
  created() {
    // URL 파라미터에 따라 초기 뷰 설정
    if (this.view) {
      switch (this.view) {
        case 'timer':
          this.currentView = 'RunTimer';
          break;
        case 'crew':
          this.currentView = 'RunWithCrew';
          break;
        case 'rank':
          this.currentView = 'RunWithRank';
          break;
        default:
          this.currentView = 'RunTimer';
      }
      this.viewHistory = [this.currentView];
    }
  },
  methods: {
    navigateTo(view) {
      this.viewHistory.push(this.currentView);
      this.currentView = view;
    },
    goBack() {
      if (this.viewHistory.length > 1) {
        this.currentView = this.viewHistory.pop();
      } else {
        this.$router.go(-1);
      }
    }
  }
}
</script>

<style scoped>
.run-container {
  min-height: 100vh;
  background-color: #f0f9f0;
}

/* 애니메이션 효과 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-leave-to, .fade-enter-from {
  opacity: 0;
}
</style>