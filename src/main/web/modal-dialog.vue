<template>
  <div v-if="show">
    <div class="modal in">
      <div class="modal-head">
        <h2>{{header}}</h2>
      </div>
      <div class="modal-body">
        <slot name="body"></slot>
      </div>
      <div class="modal-foot">
        <slot name="footer"></slot>
        <a href="#" class="button button-link" @click="close()">Cancel</a>
      </div>
    </div>
    <div class="modal-overlay in"></div>
  </div>
</template>

<script>
export default {
  props: {
    header: String,
    show: Boolean
  },
  methods: {
    close() {
      this.$emit('close');
    }
  },
  mounted() {
    document.addEventListener('keydown', (e) => {
      if (this.show && e.keyCode == 27) {
        this.close();
      }
    });
  }
};
</script>
