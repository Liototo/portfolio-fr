import numpy as np
import sys
sys.path.append('..')
from utils import label_to_onehot


class LogisticRegression(object):
    """
        LogisticRegression classifier object.
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
            The LogisticRegression class should have variables defining the learning rate (lr)
            and the number of max iterations (max_iters)
            You can either pass these as args or kwargs.
        """
        
        if 'lr' in kwargs:
            self.lr = kwargs['lr']
        elif len(args) > 0:
            self.lr = args[0]
        else:
            self.lr = 10

        if 'max_iters' in kwargs:
            self.max_iters = kwargs['max_iters']
        elif len(args) > 1:
            self.max_iters = args[1]
        else:
            self.max_iters = 1000
            
    def softmax(self, t):
        num = np.exp(t)
        den = np.sum(num, axis=1)
        return np.divide(num, np.reshape(den, (den.shape[0], 1)))
    
    def classify(self, data):
        labels = self.softmax(data)
        return np.argmax(labels, axis=1)

    def fit(self, training_data, training_labels):
        """
            Trains the model, returns predicted labels for training data.
            Arguments:
                training_data (np.array): training data of shape (N,D)
                training_labels (np.array): regression target of shape (N,)
            Returns:
                pred_labels (np.array): target of shape (N,)
        """
        def gradient(x, W, labels):

            sm = self.softmax(x @ W)
        
            gradient = x.T @ (sm - labels)

            return gradient
        
        weights = np.random.normal(0, 0.1, [training_data.shape[1], np.amax(training_labels) + 1])
        for it in range(self.max_iters):
            grad = gradient(training_data, weights, label_to_onehot(training_labels))
            weights = weights - self.lr*grad

        self.W = weights

        pred_labels = self.classify(training_data @ self.W)

        return pred_labels

    def predict(self, test_data):
        """
            Runs prediction on the test data.
            
            Arguments:
                test_data (np.array): test data of shape (N,D)
            Returns:
                test_labels (np.array): labels of shape (N,)
        """   

        pred_labels = self.classify(test_data @ self.W)

        return pred_labels
