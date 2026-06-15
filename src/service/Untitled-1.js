console.log(mgt); // Check the structure of the object
if (typeof mgt.clearMarks === 'function') {
    mgt.clearMarks();
} else {
    console.error('clearMarks is not a function on mgt');
    // Implement alternative logic here
    mgt.clearMarks = function() {
        console.log('Fallback clearMarks implementation executed.');
    };
    mgt.clearMarks();
}