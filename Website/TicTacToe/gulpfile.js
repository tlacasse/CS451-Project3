var gulp = require('gulp');

var sass = require("gulp-sass");
var cleanCSS = require("gulp-clean-css");
var concat = require("gulp-concat");
var uglify = require("gulp-uglify");
var inject = require("gulp-inject");
var angularFilesort = require("gulp-angular-filesort");
var noop = require("gulp-noop");
var minimist = require('minimist');
var rename = require('gulp-rename');

var knownOptions = {
    string: 'env',
    default: {
        env: 'dev'
    }
};

var options = minimist(process.argv.slice(2), knownOptions);

function isProduction() {
    return options.env.toUpperCase() === 'release'.toUpperCase();
}

var d = new Date();
var dateAppend = "_" + String(d.getFullYear()) + "-" + String(d.getMonth()+1) + "-" + String(d.getDate())
               + "_" + String(d.getHours())    + "-" + String(d.getMinutes()) + "-" + String(d.getSeconds());

var build = '_Build';

gulp.task('index', function () {
    return gulp.src('index.html')
        .pipe(gulp.dest(build));
});

gulp.task('views', function () {
    return gulp.src('app/**/*.html')
        .pipe(gulp.dest(build + '/views'));
});

gulp.task('css', function () {
    return gulp.src('content/styles/*.scss')
        .pipe(sass())
        .pipe(isProduction() ? concat('style.min.css') : noop())
        .pipe(isProduction() ? cleanCSS() : noop())
        .pipe(rename(function (path) {
            path.basename += dateAppend;
        }))
        .pipe(gulp.dest(build + '/styles'));
});

gulp.task('app', function () {
    return gulp.src('app/**/*.js')
        .pipe(angularFilesort())
        .pipe(isProduction() ? concat('app.min.js') : noop())
        .pipe(isProduction() ? uglify() : noop())
        .pipe(rename(function (path) {
            path.basename += dateAppend;
        }))
        .pipe(gulp.dest(build + '/app'));
});

gulp.task('angular', function () {
    return gulp.src(isProduction() ? 'content/scripts/minified/*.js' : 'content/scripts/dev/*.js')
        .pipe(angularFilesort())
        .pipe(isProduction() ? concat('angular-all.min.js') : noop())
        .pipe(gulp.dest(build + '/angular'));
});

gulp.task('inject', function () {
    return gulp.src(build + '/index.html')
        .pipe(inject(
            gulp.src(build + '/angular/*.js').pipe(angularFilesort()),
            { relative: true, name: 'ng' })
        )
        .pipe(inject(
            gulp.src(build + '/app/**/*.js').pipe(angularFilesort()),
            { relative: true, name: 'app' })
        )
        .pipe(inject(
            gulp.src(build + '/styles/*.css', { read: false }),
            { relative: true })
        )
        .pipe(gulp.dest(build));
});

gulp.task('bin', function () {
    return gulp.src('bin/*')
        .pipe(gulp.dest(build + '/bin'));
});

gulp.task('asp', function () {
    return gulp.src([
        'Global.asax',
        'ApplicationInsights.config',
        'packages.config',
        'Web.config',
        'Web.Debug.config',
        'Web.Release.config'
    ]).pipe(gulp.dest(build));
});

gulp.task('default', gulp.series('index', 'views', 'css', 'app', 'angular', 'inject', 'bin', 'asp'));