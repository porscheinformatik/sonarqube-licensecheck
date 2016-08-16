const gulp = require('gulp'),
  concat = require('gulp-concat'),
  angularFilesort = require('gulp-angular-filesort');
const vendorJs = ['node_modules/angular/angular.min.js',
  'node_modules/angular-animate/angular-animate.min.js',
  'node_modules/angular-aria/angular-aria.min.js',
  'node_modules/angular-material/angular-material.min.js'];
const compileJs = ['src/main/resources/static/**/*.js'];

gulp.task('js-vendor', function () {
  gulp.src(vendorJs)
    .pipe(concat('vendor.js'))
    .pipe(gulp.dest('target/classes/static/'));
});

gulp.task('js-compile', function () {
  gulp.src(compileJs)
    .pipe(angularFilesort())
    .pipe(concat('licensecheck.js'))
    .pipe(gulp.dest('target/classes/static/'));
});

gulp.task('default', ['js-vendor', 'js-compile']);
