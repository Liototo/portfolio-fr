import numpy as np

class KNN(object):
    """
        kNN classifier object.
        Feel free to add more functions to this class if you need.
        But make sure that __init__, set_arguments, fit and predict work correctly.
    """

    def __init__(self, *args, **kwargs):
        """
            Initialize the task_kind (see dummy_methods.py)
            and call set_arguments function of this class.
        """
        self.task_kind = 'classification'
        self.set_arguments(*args, **kwargs)

    def set_arguments(self, *args, **kwargs):
        """
            args and kwargs are super easy to use! See dummy_methods.py
            The KNN class should have a variable defining the number of neighbours (k).
            You can either pass this as an arg or a kwarg.
        """
        if 'knn_neighbours' in kwargs:
            self.knn_neighbours = kwargs['knn_neighbours']
        elif len(args) > 0: 
            self.knn_neighbours = args[0]
        else: 
            self.knn_neighbours = 0


    def euclidean_dist(self, point, training_points):
        
        dists = np.empty(training_points.shape[0])

        for i in range(dists.shape[0]):
            dists[i] = np.sqrt(np.sum(np.square(point - training_points[i])))
        
        return dists
    
    def find_k_nearest_neighbours(self, distances):
        
        indices = np.argsort(distances)[:self.knn_neighbours]

        return indices

    def predict_label(self, neighbour_labels):

        beancount = np.bincount

        return np.argmax(beancount(neighbour_labels))

    def kNN(self, unlabeled):
        
        def kNN_one(unlabeled_one):

            distances = self.euclidean_dist(unlabeled_one, self.training_data)

            nn_indices = self.find_k_nearest_neighbours(distances)

            neigbour_labels = self.training_labels[nn_indices]

            best_label = self.predict_label(neigbour_labels)

            return best_label

        return np.apply_along_axis(kNN_one, 1, unlabeled)

    

    def fit(self, training_data, training_labels):
        """
            Trains the model, returns predicted labels for training data.
            Hint: Since KNN does not really have parameters to train, you can try saving the training_data
            and training_labels as part of the class. This way, when you call the "predict" function
            with the test_data, you will have already stored the training_data and training_labels
            in the object.
            
            Arguments:
                training_data (np.array): training data of shape (N,D)
                training_labels (np.array): labels of shape (N,)
            Returns:
                pred_labels (np.array): labels of shape (N,)
        """

        self.training_data = training_data
        self.training_labels = training_labels

        pred_labels = self.kNN(training_data)

        return pred_labels
                               
    def predict(self, test_data):
        """
            Runs prediction on the test data.
            
            Arguments:
                test_data (np.array): test data of shape (N,D)
            Returns:
                test_labels (np.array): labels of shape (N,)
        """      
        
        test_labels = self.kNN(test_data)

        return test_labels